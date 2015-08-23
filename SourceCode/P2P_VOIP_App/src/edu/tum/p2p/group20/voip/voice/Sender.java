package edu.tum.p2p.group20.voip.voice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.ShortBufferException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.parser.ParseException;

import edu.tum.p2p.group20.voip.com.Message;
import edu.tum.p2p.group20.voip.com.MessageCrypto;
import edu.tum.p2p.group20.voip.com.ModuleValidator;
import edu.tum.p2p.group20.voip.config.ConfigParser;
import edu.tum.p2p.group20.voip.crypto.RSA;
import edu.tum.p2p.group20.voip.crypto.SHA2;
import edu.tum.p2p.group20.voip.dh.SessionKeyManager;

// Sender is basically a client in the TCP Client-Server paradigm.
// It knows that a receiver is listening for new connections and it can send
//	and receive messages.
// To be more precise, it is the caller, who is to call a receiver.

/**
 * Class for taking the user into CallSender state i.e., caller!
 * 
 * Typically the class which will be a thread for call control messages in case
 * of a caller - The person who is making the call
 * 
 * This class is responsible for handling call control messages and once the
 * call is connected or accepted by the other user, it informs the main
 * thread(the UI thread) which starts the threads for voice transmission. This
 * class also runs the heartbeat messages to check the call status
 * 
 * @author Sri Vishnu Totakura <srivishnu@totakura.in>, Anshul Vij
 *
 */
public class Sender {

	/**
	 * 
	 */
	private static final int HEARTBEAT_TIMEOUT = 15000;

	private Socket socket;

	private CallInitiatorListener callInitiatorListener;

	private ConfigParser configParser;

	private PrintWriter out;

	private KeyPair hostKeyPair;

	private String hostPublicKeyEncoded;

	private String hostPseudoIdentity;

	private MessageCrypto messageCrypto;

	private Date lastTimestamp;

	private Date lastHeartBeat;

	private boolean stop;

	private Thread readMessageThread;

	private Timer heartBeatTimer;
	private TimerTask heartBeatSender;

	public void initiateCall(final String otherPartyPseudoIdentity,
		RSAPublicKey otherPartyPublicKey,
		String destinationIP,
		ConfigParser parser, boolean isFakeCall) throws IllegalStateException, Exception {

		configParser = parser;
		try {

			hostKeyPair = RSA.getKeyPairFromFile(configParser.getUserHostKey());
			hostPublicKeyEncoded = Base64.encodeBase64String(hostKeyPair.getPublic().getEncoded());

			hostPseudoIdentity = Base64.encodeBase64String(
				new SHA2().makeSHA2Hash(hostKeyPair.getPublic().getEncoded())
				);

			// TODO: put soTimeout
			int inTunnelPort = configParser.getVoipPort();
					
			if(isFakeCall){
				inTunnelPort = configParser.getFakeCallPort();
			}
			socket = new Socket(
				InetAddress.getByName(destinationIP),
				inTunnelPort,
				InetAddress.getByName(configParser.getTunIP()),
				0
				);

			System.out.println("Caller: Socket opened to the destination IP");

			out = new PrintWriter(socket.getOutputStream(), true);
			final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			String inputLine;

			messageCrypto = new MessageCrypto(
				hostKeyPair,
				otherPartyPublicKey,
				hostPseudoIdentity,
				otherPartyPseudoIdentity
				);

			sendInitialModuleVerificationPing();

			// Receive ping reply
			inputLine = in.readLine();
			Message pingReplyMessage = new Message(inputLine, false, messageCrypto);
			if (!pingReplyMessage.isValid(null, null)) {
				throw new Exception("Message validation failed");
			}

			lastTimestamp = pingReplyMessage.timestamp();

			System.out.println("Sender: Recevied: " + pingReplyMessage.get("type"));

			if ("PING_REPLY_BUSY".equals(pingReplyMessage.get("type"))) {
				// the remote peer is busy
				// show this info to user and stop the call
				JOptionPane.showMessageDialog(new JFrame(), "The remote user is busy","User busy",JOptionPane.INFORMATION_MESSAGE);
				callInitiatorListener.onCallDisconnected("The remote user is busy");
				
				return;

			}

			// =======================================================================
			// Perform Diffie Helmann and obtain Session key
			// =======================================================================

			// Send DH with sender receiver.
			// Generate and initiate DH
			SessionKeyManager receiverKeyManager = SessionKeyManager.makeInitiator();

			Message dhInitMessage = new Message(messageCrypto);
			dhInitMessage.put("type", "DH_INIT");
			dhInitMessage.put("DHPublicKey", receiverKeyManager.base64PublicDHKeyString());
			out.println(dhInitMessage.asJSONStringForExchange());

			// Receive other parties DHpublickey
			inputLine = in.readLine();
			Message receivedDhMessage = new Message(inputLine, false, messageCrypto);
			if (!receivedDhMessage.isValid(lastTimestamp, "DH_REPLY")) {
				throw new Exception("Message validation failed");
			}
			String dhPublicKeyString = (String) receivedDhMessage.get("DHPublicKey");
			lastTimestamp = receivedDhMessage.timestamp();
			System.out.println(receivedDhMessage.get("type"));

			byte[] sessionKey = receiverKeyManager.makeSessionKey(dhPublicKeyString);
			messageCrypto.setSessionKey(sessionKey, false);
			System.out.println("Sender: SessionKey: " + Base64.encodeBase64String(sessionKey));

			// =======================================================================
			// Call control happens from here with encryption
			// =======================================================================
			// Send CALL_INIT.
			Message callInitMessage = new Message(messageCrypto);
			callInitMessage.put("type", "CALL_INIT");
			callInitMessage.encrypt();
			out.println(callInitMessage.asJSONStringForExchange());

			// Read CALL_INIT_ACK
			inputLine = in.readLine();
			Message receivedMessage = new Message(inputLine, true, messageCrypto);
			if (!receivedMessage.isValid(lastTimestamp, "CALL_INIT_ACK")) {
				throw new Exception("Message validation failed");
			}

			receivedMessage.decrypt();
			lastTimestamp = receivedMessage.timestamp();
			System.out.println("Sender: Received: " + receivedMessage.get("type"));

			// Show waiting to the user here!
			System.out.println("Waiting for receiver to accept the call...");

			// Read CALL_ACCEPT/ CALL_DECLINE
			inputLine = in.readLine();
			Message callAcceptMessage = new Message(inputLine, true, messageCrypto);

			if (!callAcceptMessage.isValid(lastTimestamp, null)) {
				// Invalid message
				callInitiatorListener.onCallFailed(otherPartyPseudoIdentity);
			}

			callAcceptMessage.decrypt();
			lastTimestamp = callAcceptMessage.timestamp();
			System.out.println("Sender: Received: " + callAcceptMessage.get("type"));

			if ("CALL_ACCEPT".equals(callAcceptMessage.get("type"))) {
				// call was accepted by remote party
				callInitiatorListener.onCallAccepted(otherPartyPseudoIdentity, sessionKey, destinationIP);

				readMessageThread = new Thread(new Runnable() {

					@Override
					public void run() {

						while (!stop) {

							String inputLine;
							try {
								inputLine = in.readLine();
								// TODO: a null check here will also indicate
								// broken connection
								Message msg = new Message(inputLine, true, messageCrypto);

								if (!msg.isValid(lastTimestamp, null)) {
									// Invalid message
									System.out.println("Invalid msg");
									stop = true;
									callInitiatorListener.onCallDisconnected(otherPartyPseudoIdentity);
									return;
								}
								msg.decrypt();
								lastTimestamp = msg.timestamp();
								System.out.println(msg.get("type"));
								if ("HEARTBEAT_ACK".equals(msg.get("type"))) {
									lastHeartBeat = lastTimestamp;
									System.out.println("Received HEARTBEAT_ACK");
								}
								else if ("CALL_DISCONNECT".equals(msg.get("type"))) {
									System.out.println("Received CALL_DISCONNECT");
									stop = true;
									callInitiatorListener.onCallDisconnected(otherPartyPseudoIdentity);
									shutdown();
									return;
								}
							} catch (IOException | ParseException |
								InvalidKeyException | ShortBufferException |
								IllegalBlockSizeException | BadPaddingException |
								java.text.ParseException e) {

								e.printStackTrace();
								stop = true;
								callInitiatorListener.onCallDisconnected(otherPartyPseudoIdentity);
								shutdown();
								return;
							}

						}
					}
				});
				readMessageThread.start();
				lastHeartBeat = new Date();
				heartBeatTimer = new Timer();
				heartBeatSender = new TimerTask() {

					@Override
					public void run() {

						// check heartbeat timestamp
						if ((new Date().getTime() - lastHeartBeat.getTime()) > HEARTBEAT_TIMEOUT) {
							// last heart beat too old
							this.cancel();// stop heartbeat timer task
							System.out.println("HEARTBEAT TIMEOUT");
							stop = true;
							readMessageThread = null;// cancel the read msg
														// thread
							callInitiatorListener.onCallDisconnected(otherPartyPseudoIdentity);
							shutdown();
							return;
						}
						Message heartbeat = new Message(messageCrypto);
						heartbeat.put("type", "HEARTBEAT");
						heartbeat.encrypt();
						System.out.println("Sending HEARTBEAT message");
						out.println(heartbeat.asJSONStringForExchange());
					}
				};
				// schedule heartBeat sending task after 2 sec for every 10 sec
				heartBeatTimer.schedule(heartBeatSender, 2000, 10000);

			}
			else if ("CALL_DECLINE".equals(callAcceptMessage.get("type"))) {
				// call was accepted by remote party
				System.out.println("Received CALL_DECLINE");
				callInitiatorListener.onCallDeclined(otherPartyPseudoIdentity);
			}
			else {
				System.out.println("Received Unknown Message type");
				shutdown();
			}

		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			shutdown();
		}
	}

	/**
	 * 
	 */
	private void sendInitialModuleVerificationPing() {

		ModuleValidator moduleValidator = new ModuleValidator();
		Message initialModuleCheck = new Message(messageCrypto);
		initialModuleCheck.put("type", "PING");
		initialModuleCheck.put("verificationHash", moduleValidator.digest);
		initialModuleCheck.put("verificationTimestamp", moduleValidator.timestampString);
		initialModuleCheck.put("senderPublicKey", hostPublicKeyEncoded);
		// Do not sign this message, cause the receiver won't have your
		// public key yet.
		out.println(initialModuleCheck.asJSONStringForExchange(true, false));
	}

	public void setCallInitiatorListener(
		CallInitiatorListener callInitiatorListener) {

		this.callInitiatorListener = callInitiatorListener;

	}

	public void disconnectCall() {

		// Send CALL_DISCONNECT.
		Message disconnectMsg = new Message(messageCrypto);
		disconnectMsg.put("type", "CALL_DISCONNECT");
		disconnectMsg.encrypt();
		out.println(disconnectMsg.asJSONStringForExchange());
		shutdown();
	}

	/**
	 * freeing up the resources
	 */
	private void shutdown() {

		if (heartBeatSender != null) {
			heartBeatSender.cancel();
			heartBeatSender = null;
		}
		if (heartBeatTimer != null) {
			heartBeatTimer.cancel();
			heartBeatTimer = null;
		}
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			socket = null;
		}
	}
}