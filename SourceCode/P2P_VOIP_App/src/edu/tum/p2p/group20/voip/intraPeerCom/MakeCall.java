package edu.tum.p2p.group20.voip.intraPeerCom;

import java.io.IOException;
import java.net.InetAddress;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;

import org.apache.commons.codec.binary.Base64;

import edu.tum.p2p.group20.voip.config.ConfigParser;
import edu.tum.p2p.group20.voip.crypto.RSA;
import edu.tum.p2p.group20.voip.crypto.SHA2;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.ReceivedMessage;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.dht.Get;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.kx.BuildTNOutgoing;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.kx.KxTunnelDestroy;
import edu.tum.p2p.group20.voip.voice.CallInitiatorListener;
import edu.tum.p2p.group20.voip.voice.Sender;

public class MakeCall {
	private  ReceivedMessage lastReceivedMessage;
	private  IntraPeerCommunicator dhtCommunicator;
	private  IntraPeerCommunicator kxCommunicator;
	private Sender sender;
	private CallInitiatorListener callInitiatorListener;
	private ConfigParser configParser;
	private byte[] hostPseudoId;
	private MakeCallEventListener errorListener;
	public void makeCall(String calleeId, ConfigParser configParser, boolean isFakeCall)
			throws Exception {

		this.configParser = configParser;

		try {
			//showing status update to user
			callInitiatorListener.onCallInitiated(calleeId);
			try{
				dhtCommunicator = new IntraPeerCommunicator(
					configParser.getDhtHost(), configParser.getDhtPort());
			} catch(IOException e){
				errorListener.onMakeCallError("Cannot connect to DHT module."
						+"\nPlease check if DHT running as per config file.");
				e.printStackTrace();
				return;
			}
			try{
			kxCommunicator = new IntraPeerCommunicator(
					configParser.getKxhost(), configParser.getKxPort());
			} catch(IOException e){
				errorListener.onMakeCallError("Cannot connect to KX modulde."
						+"\nPlease check if KX module running as per config file.");
				e.printStackTrace();
				return;
			}
			KeyPair hostKeyPair=null;
			try{
				hostKeyPair = RSA.getKeyPairFromFile(configParser
					.getUserHostKey());
			} catch(IOException e) {
				errorListener.onMakeCallError("Error with reading user key file."
						+"\nPlease check if path is correct in config file.");
				e.printStackTrace();
				return;
			}
			PublicKey hostPubKey = hostKeyPair.getPublic();
			
			SHA2 sha2 = new SHA2();
			hostPseudoId = sha2.makeSHA2Hash(hostPubKey.getEncoded());
			// CalleeId is the string entered by the user
			byte[] calledPseudoIdByte = Base64.decodeBase64(calleeId); 

			// ================================================================
			// Get callee's information from DHT_GET
			// ================================================================
			Get getMessage = new Get(calledPseudoIdByte);
			dhtCommunicator.sendMessage(getMessage);
			lastReceivedMessage = dhtCommunicator.readIncomingAndHandleError();

			if (!dhtCommunicator.isValidMessage(lastReceivedMessage,
					"MSG_DHT_GET_REPLY", calledPseudoIdByte)) {
				throw new Exception("GET reply error");
			}
			// Parse DHT_GET_REPLY for data like publicKey and exchange point
			// info
			byte[] publickKeyBytes = lastReceivedMessage.get("publicKey");
			RSAPublicKey remotePublicKey = RSA
					.getPublicKeyFromBytes(publickKeyBytes);
			byte[] xchangePointInfoForKx = lastReceivedMessage
					.get("xchangePointInfoForKx");

			// ================================================================
			// Build out going tunnel to callee
			// ================================================================
			sendKxBuildOutgoingTunnel(hostPseudoId, xchangePointInfoForKx);
			lastReceivedMessage = kxCommunicator.readIncomingAndHandleError();

			if (kxCommunicator.isValidMessage(lastReceivedMessage,
					"MSG_KX_TN_READY", hostPseudoId)) {
				// check for null pointer
				InetAddress destinationIpv4 = InetAddress
						.getByAddress(lastReceivedMessage.get("ipv4"));
				InetAddress destinationIpv6 = InetAddress
						.getByAddress(lastReceivedMessage.get("ipv6"));
				System.out.println("Now connect to: " + destinationIpv4);
				System.out.println("Now connect to: " + destinationIpv6);

				// create the object for sender module
				sender = new Sender();
				sender.setCallInitiatorListener(callInitiatorListener);
				// initiate sender with destination point info
				sender.initiateCall(calleeId, remotePublicKey,
						destinationIpv4.getHostAddress(), configParser, isFakeCall);
			}
			else {
				errorListener.onMakeCallError("Cannot create Outgoing Tunnel"
						+"\nInvalid message was returned from KX");
				System.out.println("Cannot create Outgoing Tunnel"
						+"\nInvalid message was returned from KX");
			}

		} catch (IOException e) {
			callInitiatorListener.onCallFailed(calleeId);
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	private void sendKxBuildOutgoingTunnel(byte[] pseudoId,
			byte[] xchangePointInfo) throws IOException {

		BuildTNOutgoing buildTnMessage = new BuildTNOutgoing(3, pseudoId,
				xchangePointInfo);
		kxCommunicator.sendMessage(buildTnMessage);
	}

	public CallInitiatorListener getCallInitiatorListener() {

		return callInitiatorListener;
	}

	public void setCallInitiatorListener(
			CallInitiatorListener callInitiatorListener) {

		this.callInitiatorListener = callInitiatorListener;
	}

	public void disconnectCall() {

		if (sender != null) {
			sender.disconnectCall();
			sender=null;
		}

		if (dhtCommunicator != null) {
			dhtCommunicator = null;
		}
		
		if (kxCommunicator != null) {
			
			KxTunnelDestroy tnDestroy = new KxTunnelDestroy(hostPseudoId);
			try {
				kxCommunicator.sendMessage(tnDestroy);
			} catch (IOException e) {
				System.out.println("Makecall: Problem with sending Tunnel Destroy");
				e.printStackTrace();
			}
			kxCommunicator = null;
		}

	}

	/**
	 * Set the error listener callback to propagate error to listener
	 * @param errorListener
	 */
	public void setErrorListener(MakeCallEventListener errorListener) {
		this.errorListener = errorListener;
	}
}
