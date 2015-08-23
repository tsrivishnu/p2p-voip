package edu.tum.p2p.group20.voip.intraPeerCom;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.commons.codec.binary.Base64;

import edu.tum.p2p.group20.voip.config.ConfigParser;
import edu.tum.p2p.group20.voip.crypto.RSA;
import edu.tum.p2p.group20.voip.crypto.SHA2;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.ReceivedMessage;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.dht.Get;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.dht.Put;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.dht.Trace;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.kx.BuildTNIncoming;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.kx.KxTunnelDestroy;
import edu.tum.p2p.group20.voip.voice.CallReceiverListener;
import edu.tum.p2p.group20.voip.voice.Receiver;

// TODO handle KX_TN_DESTROY when the process has to be killed
/**
 * Class responsible for taking the user online in the app. It makes him a
 * callee waiting for calls. Process goes as follows It find a random pseudo Id
 * for exchange point Builds an incoming tunnel Starts a thread to listen for
 * calls on that thread.
 * 
 * @author Sri Vishnu Totakura <srivishnu@totakura.in>, Anshul Vij
 *
 */
public class GoOnline implements CallReceiverListener {

	public static ReceivedMessage lastReceivedMessage;
	// TODO: move the method calls to DHT and KX wrapper
	public static IntraPeerCommunicator dhtCommunicator;
	public static IntraPeerCommunicator kxCommunicator;
	private GoOnlineEventListener eventListener;
	private CallReceiverListener callReceiverListener;
	private Receiver receiverThread;
	private ConfigParser configParser;
	private ServerSocket serverSocket;
	private boolean stop;
	private byte[] pseduoIdBytes;

	public static void main(String[] args) throws Exception {

		GoOnline goOnline = new GoOnline();
		if (args.length != 2) {
			System.err.println("Usage: java Sender -c <config>");
			System.exit(1);
			return;
		}

		goOnline.goOnline(ConfigParser.getInstance(args[1]));
	}

	public void goOnline(final ConfigParser configParser) throws Exception {

		this.configParser = configParser;

		try {
			// Make separate communicator for DHT and KX module as they run on
			// different host

			// Communicator for DHT
			try {

				dhtCommunicator = new IntraPeerCommunicator(
					configParser.getDhtHost(),
					configParser.getDhtPort()
					);
			} catch (UnknownHostException e) {

				JOptionPane.showMessageDialog(
					new JFrame(),
					"Check DHT host address"
					);
				e.printStackTrace();

				return;
			} catch (IOException e) {

				JOptionPane.showMessageDialog(
					new JFrame(),
					"Check if DHT module is running as per config "
					);
				e.printStackTrace();

				return;
			}

			// Communicator for KX
			try {

				kxCommunicator = new IntraPeerCommunicator(
					configParser.getKxhost(),
					configParser.getKxPort()
					);
			} catch (UnknownHostException e) {

				JOptionPane.showMessageDialog(new JFrame(),
					"Check KX host address");
				e.printStackTrace();

				return;
			} catch (IOException e) {

				JOptionPane.showMessageDialog(new JFrame(),
					"Check if KX module is running as per config ");
				e.printStackTrace();

				return;
			}

			KeyPair hostKeyPair = RSA.getKeyPairFromFile(
				configParser.getUserHostKey()
				);

			PublicKey rsaPublicKey = hostKeyPair.getPublic();
			SHA2 sha2 = new SHA2();
			// PseudoID is the hash of the publickey.
			pseduoIdBytes = sha2.makeSHA2Hash(rsaPublicKey.getEncoded());

			// To user we give the Base64 encoded string of the pseudoId so that
			// he can copy it as a string and send to whomever he wants
			String hostPseudoIdentity = Base64
				.encodeBase64String(pseduoIdBytes);
			// Show the pseudoId to the user
			eventListener.onPseudoIdReady(hostPseudoIdentity);

			// ================================================================
			// Find random Exchange point
			// ================================================================
			byte[] randomPsuedoId = null;

			// finding a exchange point.
			boolean isRandomPseudoIdChosen = false;

			// Find a randomPseudoId and check if it exists, if is doesn't
			// that is the id we need
			while (!isRandomPseudoIdChosen) {
				// Pick a random pseudo id
				randomPsuedoId = sha2.makeSHA2Hash(new java.util.Date()
					.toString().getBytes());
				// Do a DHT_GET to find if that id exists
				Get dhtGet = new Get(randomPsuedoId);
				System.out.println("Sending DHT_GET for randomID");
				dhtCommunicator.sendMessage(dhtGet);
				lastReceivedMessage = dhtCommunicator
					.readIncomingAndHandleError();
				// When either message is not received or message is not a valid
				// reply,
				// we have a random not existing pseudoId
				// If message is a valid reply, that means the pseudo id exists.
				if (lastReceivedMessage == null
					|| !dhtGet.isValidReply(lastReceivedMessage)) {

					isRandomPseudoIdChosen = true;
					System.out.println("Found a random Pseudo ID");
				}
			}

			byte[] xchangePointInfoFromTrace = doDhtTraceForRandomExchangePoint(randomPsuedoId);
			byte[] xChangePointInfoForKx = Helper
				.trasnformXChangePointInfoFromDhtToKx(xchangePointInfoFromTrace);

			// ================================================================
			// Build KX Tunnel
			// ================================================================
			sendKxBuildIncomingTunnel(pseduoIdBytes, xChangePointInfoForKx);
			lastReceivedMessage = kxCommunicator.readIncomingAndHandleError();

			if (kxCommunicator.isValidMessage(
				lastReceivedMessage,
				"MSG_KX_TN_READY", pseduoIdBytes
				)) {

				// get this ip from the config file
				String inTunnelIP = configParser.getTunIP();
				// We choose this port from config file
				int inTunnelPort = configParser.getVoipPort();
				try {

					serverSocket = new ServerSocket(
						inTunnelPort,
						1,
						InetAddress.getByName(inTunnelIP)
						);

					// Create a call receiver thread which runs in background to
					// accept calls.
					new Thread(new Runnable() {

						@Override
						public void run() {

							while (!stop) {
								try {
									Socket clientSocket = serverSocket.accept();
									// creating a listener on incoming tunnel
									final Receiver receiver = new Receiver(
										clientSocket,
										configParser,
										GoOnline.this
										);
									receiver.start();

								} catch (IOException e) {
									if (!stop) {
										eventListener.onException(e);
										e.printStackTrace();
									}
								}

							}

						}
					}).start();
				} catch (IOException e) {
					if (!stop) {
						eventListener.onException(e);
						e.printStackTrace();
					}
				}

				sendDhtPutMessage(
					pseduoIdBytes,
					hostKeyPair,
					xChangePointInfoForKx);

				System.out.println("You are now online!");
				if (eventListener != null) {
					eventListener.onOnline();
				}
				return;
			}
			else {

				System.out.println("Offline!");
				if (eventListener != null) {
					eventListener.onOffline();
				}
				return;

			}
		} catch (IOException e) {

			if(!stop) {
				System.out
				.println("Exception caught when trying to connect on port "
					+ configParser);
				System.out.println(e.getMessage());
				e.printStackTrace();
				if (eventListener != null) {
					eventListener.onException(e);
				}	
			}
			
			return;
		} catch (Exception e) {

			System.err
				.println("Exception caught when trying to connect on port "
					+ configParser);
			System.err.println(e.getMessage());
			e.printStackTrace();

			if (eventListener != null) {
				eventListener.onException(e);
			}

			return;
		}
	}

	public GoOnlineEventListener getEventListener() {

		return eventListener;
	}

	public void setEventListener(GoOnlineEventListener eventListener) {

		this.eventListener = eventListener;
	}

	/**
	 * Performs DHT Trace with given pseudoId
	 * 
	 * @param pseudoIdToTrace
	 * @return exchangePoint info from the DHT_TRACE_REPLY
	 * @throws Exception
	 */
	private static byte[] doDhtTraceForRandomExchangePoint(
		byte[] pseudoIdToTrace) throws Exception {

		sendDhtTraceMessage(pseudoIdToTrace);
		lastReceivedMessage = dhtCommunicator.readIncomingAndHandleError();

		// if no reply is received, or received a wrong type raise exception
		if (lastReceivedMessage == null) {
			throw new Exception("DHT trace reply timeout error");
		}

		System.out.println(lastReceivedMessage.name());

		if (!dhtCommunicator.isValidMessage(lastReceivedMessage,
			"MSG_DHT_TRACE_REPLY", pseudoIdToTrace)) {
			throw new Exception("DHT trace reply error");
		}

		return lastReceivedMessage.get("xchangePointInfo");
	}

	/**
	 * Performs a DHT_PUT for give pseudoId and values
	 * 
	 * @param forPseudoId
	 * @param rsaKeyPair
	 * @param xchangePointInfo
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 * @throws NoSuchAlgorithmException
	 */
	private static void sendDhtPutMessage(byte[] forPseudoId,
		KeyPair rsaKeyPair, byte[] xchangePointInfo) throws IOException,
		InvalidKeyException, SignatureException, NoSuchAlgorithmException {

		Put put_message = new Put(forPseudoId, (short) 12, 255, rsaKeyPair,
			xchangePointInfo);
		dhtCommunicator.sendMessage(put_message);
	}

	/**
	 * Send a DHT_TRACE for a given pseudoId
	 * 
	 * @param forPseudoId
	 * @throws IOException
	 */
	private static void sendDhtTraceMessage(byte[] forPseudoId)
		throws IOException {

		Trace traceMessage = new Trace(forPseudoId);
		dhtCommunicator.sendMessage(traceMessage);
	}

	/**
	 * Sends KX_BUILD_INCOMING message with give pseudoId and exchange point
	 * info
	 * 
	 * @param pseudoId
	 * @param xchangePointInfo
	 * @throws IOException
	 */
	public static void sendKxBuildIncomingTunnel(byte[] pseudoId,
		byte[] xchangePointInfo) throws IOException {

		BuildTNIncoming buildTnMessage = new BuildTNIncoming(3, pseudoId,
			xchangePointInfo);
		System.out.println("sendKxBuildIncomingTunnel:");
		kxCommunicator.sendMessage(buildTnMessage);
	}

	@Override
	public boolean onIncomingCall(String pseudoId, byte[] sessionKey) {

		return callReceiverListener.onIncomingCall(pseudoId, sessionKey);
	}

	@Override
	public void onIncomingCallConnected(String pseudoId, Receiver receiver, byte[] sessionKey) {

		callReceiverListener.onIncomingCallConnected(pseudoId, receiver, sessionKey);
	}

	@Override
	public void onCallDisconnected(String pseudoId) {

		callReceiverListener.onCallDisconnected(pseudoId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.tum.p2p.group20.voip.voice.CallReceiverListener#onDestinationIPReady
	 * (java.lang.String)
	 */
	@Override
	public void onDestinationIPReady(String destinationIP) {

		callReceiverListener.onDestinationIPReady(destinationIP);

	}

	public CallReceiverListener getCallReceiverListener() {

		return callReceiverListener;
	}

	/**
	 * Register a CallReceiverListern to handle call notifications
	 * 
	 * @param callReceiverListener
	 */
	public void setCallReceiverListener(
		CallReceiverListener callReceiverListener) {

		this.callReceiverListener = callReceiverListener;
	}
	
	/**
	 * Terminate the incoming tunnel
	 */
	
	public void goOffline(){
		stop=true;
		if(serverSocket!=null){
			try {
				serverSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			serverSocket=null;
			
			KxTunnelDestroy tnDestroy = new KxTunnelDestroy(pseduoIdBytes);
			
			try {
				kxCommunicator.sendMessage(tnDestroy);
			} catch (IOException e) {
				System.out.println("Makecall: Problem with sending Tunnel Destroy");
				e.printStackTrace();
			}
			
			eventListener.onOffline();
		}
	}
}
