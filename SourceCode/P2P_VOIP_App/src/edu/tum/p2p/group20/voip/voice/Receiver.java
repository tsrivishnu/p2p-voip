package edu.tum.p2p.group20.voip.voice;

import java.net.*;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.io.*;

import org.apache.commons.codec.binary.Base64;

import edu.tum.p2p.group20.voip.com.Message;
import edu.tum.p2p.group20.voip.com.MessageCrypto;
import edu.tum.p2p.group20.voip.com.ModuleValidator;
import edu.tum.p2p.group20.voip.config.ConfigParser;
import edu.tum.p2p.group20.voip.crypto.RSA;
import edu.tum.p2p.group20.voip.crypto.SHA2;
import edu.tum.p2p.group20.voip.dh.SessionKeyManager;

/**
 * Class for taking the user into CallReceiver state
 * it listens on the TUN_IP and VoipPort for call PINGs
 * and request.
 * 
 * 
 * @authors Anshul Vij <anshul.vij@tum.de>, Sri Vishnu Totakura
 *          <srivishnu@totakura.in>
 *
 */
// Receiver is basically a Server in the TCP Client-Server paradigm.
// It listens to receive messages on a socket.
// To be more precise, it is the callee, who is ready to receive calls.

public class Receiver extends Thread {

	// Listener for call notifications
	private CallReceiverListener callReceiverListener;
	private Socket clientSocket;// socket if some-remote party tries to make a
	private int portNumber;
	private boolean stop;// flag to quit the thread loop
	private static int status = 0;// status of the receiver
	private final static int IDLE = 0;// no incoming call
	private final static int BUSY = 1;// establishing incoming call
	private final static int WAIT = 2;// incoming call is already existing
	private static int connectedCallsCount = 0;// use this to reply with BUSY
												// message
	protected static final long HEARTBEAT_TIMEOUT = 15000; // 15sec timeout
	private ConfigParser configParser;
	private Date lastHeartBeat;
	private String otherPartyPseudoIdentity;
	private MessageCrypto messageCrypto;
	private ModuleValidator moduleValidator;
	private PrintWriter out;
	private Timer heartBeatTimer;
	private TimerTask heartBeatSender;

	/**
	 * @param clientSocket2
	 */
	public Receiver(
					Socket socket,
					ConfigParser parser,
					CallReceiverListener listener) {

		clientSocket = socket;
		configParser = parser;
		callReceiverListener = listener;
	}

	@Override
	public void run() {

		try {

			String inputLine;
			
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(
				new InputStreamReader(clientSocket.getInputStream())
			);

			// Get host's pseudoId
			// Hash the public key of hostKeyPair to get hostPseudoIdentity
			KeyPair hostKeyPair = RSA.getKeyPairFromFile(
				configParser.getUserHostKey()
			);
			String hostPseudoIdentity = Base64.encodeBase64String(
				new SHA2().makeSHA2Hash(hostKeyPair.getPublic().getEncoded())
			);
			
			// We don't know the caller details yet! 
			//	We will get them from the PING message
			PublicKey otherPartyPublicKey = null; 
			otherPartyPseudoIdentity = null;

			messageCrypto = new MessageCrypto(
				hostKeyPair,
				otherPartyPublicKey,
				hostPseudoIdentity,
				otherPartyPseudoIdentity
			);

			Date lastTimestamp;

			inputLine = in.readLine();				
			// Receive a module verification PING
			Message receivedPingMessage = new Message(
				inputLine,
				false,
				messageCrypto
			);
			
			// Validate the module
			boolean isValidModule = new ModuleValidator(
				(String) receivedPingMessage.get("verificationTimestamp"),
				(String) receivedPingMessage.get("verificationHash")
			).isValid();
			
			if (!isValidModule) {
				// TODO: add log4j here
				// quiting this thread
				clientSocket.close();
				return;
			}
			System.out.println(receivedPingMessage.get("type"));

			// Learn caller's public key and pseudoIdentity
			otherPartyPseudoIdentity = (String) receivedPingMessage
													.get("sender");
			System.out.println("PING from: " + otherPartyPseudoIdentity);

			messageCrypto.otherPartyPseudoIdentity = otherPartyPseudoIdentity;
			messageCrypto.otherPartyPublicKey = RSA.getPublicKeyFromString(
				(String) receivedPingMessage.get("senderPublicKey")
			);
			
			lastTimestamp = receivedPingMessage.timestamp();

			if (status == IDLE) {
				// If there are no active calls
				
				// Send PING_REPLY with module verification					
				moduleValidator = new ModuleValidator();
				Message pingReply = new Message(messageCrypto);
				pingReply.put("type", "PING_REPLY");
				pingReply.put("verificationHash", moduleValidator.digest);
				pingReply.put(
					"verificationTimestamp",
					moduleValidator.timestampString
				);
				out.println(pingReply.asJSONStringForExchange());
				
				
				// ========================================================
				// Perform Diffie Helmann and obtain Session key
				// ========================================================
				// Receive other parties DHPublicKey data

				inputLine = in.readLine();				
				
				Message receivedDhMessage = new Message(
					inputLine,
					false,
					messageCrypto
				);
				if (!receivedDhMessage.isValid(lastTimestamp, "DH_INIT")) {
					// TODO: add log4j here
					// quiting this thread
					clientSocket.close();
					return;
					// throw new Exception("Message validation failed");
				}
				System.out.println(receivedDhMessage.get("type"));
				String senderPublicKeyString = (String) receivedDhMessage
													.get("DHPublicKey");

				SessionKeyManager receiverKeyManager = SessionKeyManager
								.makeSecondParty(senderPublicKeyString);
				
				byte[] sessionKey = receiverKeyManager
									.makeSessionKey(senderPublicKeyString);

				System.out.println("SessionKey: "
					+ Base64.encodeBase64String(sessionKey));

				messageCrypto.setSessionKey(sessionKey, false);

				// Send your dh params to the other party.
				sendDhReply(receiverKeyManager);

				// ========================================================
				// Call Control messages from here
				// ========================================================

				// Read CALL_INIT
				inputLine = in.readLine();
				Message receivedMessage = new Message(
					inputLine,
					true,
					messageCrypto
				);				
				if (!receivedMessage.isValid(lastTimestamp, "CALL_INIT")) {
					// TODO: add log4j here
					// quiting this thread
					clientSocket.close();
					return;
				}
				receivedMessage.decrypt();
				lastTimestamp = receivedMessage.timestamp();
				System.out.println(receivedMessage.get("type"));

				// Send CALL_INIT_ACK.
				sendCallInitAck();

				// Show call accept dialogue to the user
				boolean accept = callReceiverListener.onIncomingCall(
					otherPartyPseudoIdentity,
					sessionKey
				);

				if (accept) {

					sendCallAccept();	
					// send call connected callback with this thread's
					// instance
					// thread instance will be used to disconnect call by
					// user input
					callReceiverListener.onIncomingCallConnected(
						otherPartyPseudoIdentity,
						this,
						sessionKey
					);

					startHeartBeatTimer();
					
					while (!stop) {
						// Read disconnect or Heartbeat message
						inputLine = in.readLine();
						
						// When connection is broken, Disconnect call
						if (inputLine == null) {
							handleCallDisconnection("Connection broken");
							shutdown();
							return;
						}
						
						// Receive a message
						Message newMessage = new Message(
							inputLine,
							true,
							messageCrypto
						);
						
						if (!newMessage.isValid(lastTimestamp, null)) {
							
							handleCallDisconnection("Invalid message received from client!");	
							shutdown();
							return;
						} else {
							// When the message is valid
							newMessage.decrypt();
							lastTimestamp = newMessage.timestamp();
							System.out.println(newMessage.get("type"));

							switch ((String) newMessage.get("type")) {
								case "HEARTBEAT":
									handleReceivedHeartBeatMessage(lastTimestamp);
									break;
								
								case "CALL_DISCONNECT":
									handleCallDisconnection("Remote disconnected");
									return;
								
								default:
									handleCallDisconnection("Unexpected Message");
									return;
							}
						}
					}
				} else {

					sendCallDecline();
					stop = true;
					return;
				}
			}
			else if (status == BUSY) {
				// Send busy message and let remote user close the socket
				sendPingBusyReply();
				stop = true;
				// close connection
				if (clientSocket != null) {
					clientSocket.close();
				}

				return;
			}
		} catch (IOException e) {
			System.out
				.println("Exception caught when trying to listen on port "
					+ portNumber + " or listening for a connection");
			System.out.println(e.getMessage());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	private void sendCallDecline() {

		Message callAcceptMessage = new Message(messageCrypto);
		callAcceptMessage.put("type", "CALL_DECLINE");
		callAcceptMessage.encrypt();
		out.println(callAcceptMessage.asJSONStringForExchange());
	}

	/**
	 * @throws NoSuchAlgorithmException
	 */
	private void sendPingBusyReply() {

		moduleValidator = new ModuleValidator();
		Message pingReply = new Message(messageCrypto);
		pingReply.put("type", "PING_REPLY_BUSY");
		pingReply.put("verificationHash", moduleValidator.digest);
		pingReply.put(
			"verificationTimestamp",
			moduleValidator.timestampString);
		out.println(pingReply.asJSONStringForExchange());
	}

	/**
	 * @param receiverKeyManager
	 */
	private void sendDhReply(SessionKeyManager receiverKeyManager) {

		Message dhPublicMessage = new Message(messageCrypto);
		dhPublicMessage.put("type", "DH_REPLY");
		dhPublicMessage.put(
			"DHPublicKey",
			receiverKeyManager.base64PublicDHKeyString()
		);
		out.println(dhPublicMessage.asJSONStringForExchange());
	}

	private void sendCallInitAck() {

		Message callInitAckMessage = new Message(messageCrypto);
		callInitAckMessage.put("type", "CALL_INIT_ACK");
		callInitAckMessage.encrypt();
		out.println(callInitAckMessage.asJSONStringForExchange());
	}

	private void sendCallAccept() {

		Message callAcceptMessage = new Message(messageCrypto);
		callAcceptMessage.put("type", "CALL_ACCEPT");
		callAcceptMessage.encrypt();
		out.println(callAcceptMessage.asJSONStringForExchange());
	}

	/**
	 * Handles received Heartbeat.
	 * 	sets the lastReceived timestamp for heartbeat
	 * 	sends a heartbeat ackownledgement HEARTBEAT_ACK
	 * 
	 */
	private void handleReceivedHeartBeatMessage(Date lastTimestamp) {

		// update last heartbeat timestamp
		lastHeartBeat = lastTimestamp;
		// send heartbeat acknowledgement
		Message heartbeatAck = new Message(
			messageCrypto
		);
		heartbeatAck.put("type", "HEARTBEAT_ACK");
		heartbeatAck.encrypt();
		out.println(
			heartbeatAck.asJSONStringForExchange()
		);		
	}

	/**
	 * @param string
	 */
	private void handleCallDisconnection(String disconnectionMessage) {
		stop = true;
		callReceiverListener
			.onCallDisconnected(disconnectionMessage);	
		shutdown();
	}

	/**
	 * 
	 */
	private void startHeartBeatTimer() {
		
		// create a timertask to check heartbeat timestamps
		lastHeartBeat = new Date();
		heartBeatTimer = new Timer();
		heartBeatSender = new TimerTask() {

			@Override
			public void run() {

				// check heartbeat timestamp
				if ((new Date().getTime() - lastHeartBeat
					.getTime()) > HEARTBEAT_TIMEOUT) {
					// last heart beat too old
					this.cancel();// stop heartbeat timer task
					System.out.println("HEARTBEAT TIMEOUT");
					stop = true;
					callReceiverListener
						.onCallDisconnected(otherPartyPseudoIdentity);
					shutdown();
					return;
				}

			}
		};
		// schedule heartBeat checking timer after 4 sec for
		// every 10 sec
		heartBeatTimer.schedule(heartBeatSender, 4000, 10000);		
	}

	public void disconnectCall() {

		// Send disconnect message to clientSocket
		Message disconnectMsg = new Message(messageCrypto);
		disconnectMsg.put("type", "CALL_DISCONNECT");
		disconnectMsg.encrypt();
		out.println(disconnectMsg.asJSONStringForExchange());
		out.flush();

		stop = true;
		shutdown();
	}
	
	/**
	 * freeing up the resources
	 */
	private void shutdown(){
		if(heartBeatSender!=null){
			heartBeatSender.cancel();
			heartBeatSender=null;
		}
		if(heartBeatTimer!=null){
			heartBeatTimer.cancel();
			heartBeatTimer=null;
		}
		if (clientSocket != null) {
			try {
				clientSocket.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			clientSocket = null;
		}
	}
}