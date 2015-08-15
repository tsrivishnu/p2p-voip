package edu.tum.p2p.group20.voip.intraPeerCom;

import java.io.IOException;
import java.net.InetAddress;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

import edu.tum.p2p.group20.voip.crypto.RSA;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.ReceivedMessage;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.dht.Get;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.dht.Put;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.dht.Trace;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.dht.TraceReply;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.kx.BuildTNIncoming;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.kx.TnReady;
import edu.tum.p2p.group20.voip.voice.CallReceiverListener;
import edu.tum.p2p.group20.voip.voice.Receiver;

// TODO handle KX_TN_DESTROY when the process has to be killed
public class GoOnline implements CallReceiverListener{

	public static ReceivedMessage lastReceivedMessage;
	public static IntraPeerCommunicator communicator;
	private GoOnlineEventListener eventListener;
	private CallReceiverListener callReceiverListener;
	private Thread receiverThread;


	public static void main(String[] args) throws Exception {

		GoOnline goOnline = new GoOnline();
		if (args.length != 1) {
			System.err.println("Usage: java Sender <port number>");
			System.exit(1);
		}

		int portNumber = Integer.parseInt(args[0]);
		goOnline.goOnline(portNumber);

	}
	
	

	public boolean goOnline(int portNumber) throws Exception {

		try {

			communicator = new IntraPeerCommunicator("127.0.0.1", portNumber);

			KeyPair hostKeyPair = RSA
					.getKeyPairFromFile("lib/receiver_private.pem");
			String hostPseudoIdentity = "9caf4058012a33048ca50550e8e32285c86c8f3013091ff7ae8c5ea2519c860c";

			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(hostPseudoIdentity.getBytes());
			byte[] key = messageDigest.digest();
			byte[] randomPsuedoId = null;

			// finding a exchange point.
			boolean isRandomPseudoIdChosen = false;
			// TODO what if you are never able to find a random non existing
			// pseudo id?
			// this loop continues for ever?
			while (!isRandomPseudoIdChosen) {
				// Pick a random pseudo id
				randomPsuedoId = messageDigest.digest(new java.util.Date()
						.toString().getBytes());
				// Do a DHT_GET to find if that id exists
				Get dhtGet = new Get(randomPsuedoId);
				System.out.println("Sending DHT_GET for randomID");
				communicator.sendMessage(dhtGet);
				lastReceivedMessage = communicator.readIncomingAndHandleError();
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

			// Send request to KX to build tunnel
			sendKxBuildIncomingTunnel(key, xChangePointInfoForKx);
			lastReceivedMessage = communicator.readIncomingAndHandleError();

			if (communicator.isValidMessage(lastReceivedMessage,
					TnReady.messageName, key)) {			
				
				//TODO: get this ip from the config file
				String inTunnelIP = "localhost";
				//TODO: We choose this port from config file 
				int inTunnelPort = 7000;
				
				//TODO: Need to create a Receiver here
				final Receiver receiver = new Receiver();
				receiver.init(InetAddress.getByName(inTunnelIP), inTunnelPort);
				
				receiverThread = new Thread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						try {
							receiver.waitForCall();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
				
				receiverThread.start();
				
				sendDhtPutMessage(key, hostKeyPair, xChangePointInfoForKx);
				lastReceivedMessage = communicator.readIncomingAndHandleError(); 
				// This is just to see if you would get any error
				
				System.out.println("You are now online!");
				if(eventListener!=null){
					eventListener.onConnected();
				}
				return true;
			} else {
				System.out.println("Offline!");
				if(eventListener!=null){
					eventListener.onDisonnected();
				}
				return false;

			}
		} catch (IOException e) {
			System.out
					.println("Exception caught when trying to connect on port "
							+ portNumber);
			System.out.println(e.getMessage());
			e.printStackTrace();
			if(eventListener!=null){
				eventListener.onException(e);
			}
			return false;
		} catch (Exception e) {
			System.err
					.println("Exception caught when trying to connect on port "
							+ portNumber);
			System.err.println(e.getMessage());
			e.printStackTrace();
			if(eventListener!=null){
				eventListener.onException(e);
			}
			return false;
		}
	}
	
	public GoOnlineEventListener getEventListener() {
		return eventListener;
	}



	public void setEventListener(GoOnlineEventListener eventListener) {
		this.eventListener = eventListener;
	}


	private static byte[] doDhtTraceForRandomExchangePoint(byte[] key)
			throws Exception {
		sendDhtTraceMessage(key);
		lastReceivedMessage = communicator.readIncomingAndHandleError();
		// if no reply is received, or received a wrong type raise exception
		System.out.println(lastReceivedMessage);
		if (lastReceivedMessage == null) {
			throw new Exception("DHT trace reply timeout error");
		}
		System.out.println(lastReceivedMessage.name());
		if (!communicator.isValidMessage(lastReceivedMessage,
				TraceReply.messageName, key)) {
			throw new Exception("DHT trace reply error");
		}

		return lastReceivedMessage.get("xchangePointInfo");
	}

	private static void sendDhtPutMessage(byte[] key, KeyPair rsaKeyPair,
			byte[] xchangePointInfo) throws IOException, InvalidKeyException,
			SignatureException, NoSuchAlgorithmException {
		Put put_message = new Put(key, (short) 12, 255, rsaKeyPair,
				xchangePointInfo);
		communicator.sendMessage(put_message);
	}

	private static void sendDhtTraceMessage(byte[] key) throws IOException {
		Trace traceMessage = new Trace(key);
		communicator.sendMessage(traceMessage);
	}

	public static void sendKxBuildIncomingTunnel(byte[] pseudoId,
			byte[] xchangePointInfo) throws IOException {
		BuildTNIncoming buildTnMessage = new BuildTNIncoming(3, pseudoId,
				xchangePointInfo);
		communicator.sendMessage(buildTnMessage);
	}



	@Override
	public boolean onIncomingCall(String pseudoId) {
		// TODO Auto-generated method stub
		return true;
	}



	@Override
	public void onCallConnected(String pseudoId) {
		// TODO Auto-generated method stub
		
	}






	@Override
	public void onCallDisconnected(String psudoId) {
		// TODO Auto-generated method stub
		
	}



	public CallReceiverListener getCallReceiverListener() {
		return callReceiverListener;
	}


	/**
	 * Register a CallReceiverListern to handle call notifications
	 * @param callReceiverListener
	 */
	public void setCallReceiverListener(CallReceiverListener callReceiverListener) {
		this.callReceiverListener = callReceiverListener;
	}
}
