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
import edu.tum.p2p.group20.voip.intraPeerCom.messages.dht.GetReply;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.kx.BuildTNOutgoing;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.kx.TnReady;
import edu.tum.p2p.group20.voip.voice.CallInitiatorListener;
import edu.tum.p2p.group20.voip.voice.Sender;

// TODO handle KX_TN_DESTROY when the process has to be killled
public class MakeCall {
	private  ReceivedMessage lastReceivedMessage;
	private  IntraPeerCommunicator dhtCommunicator;
	private  IntraPeerCommunicator kxCommunicator;
	private Sender sender;
	private CallInitiatorListener callInitiatorListener;
	private ConfigParser configParser;

	public void makeCall(String calleeId, ConfigParser configParser)
			throws Exception {

		this.configParser = configParser;

		try {
			//showing status update to user
			callInitiatorListener.onCallInitiated(calleeId);
			dhtCommunicator = new IntraPeerCommunicator(
					configParser.getDhtHost(), configParser.getDhtPort());
			kxCommunicator = new IntraPeerCommunicator(
					configParser.getKxhost(), configParser.getKxPort());

			KeyPair hostKeyPair = RSA.getKeyPairFromFile(configParser
					.getHostKey());
			PublicKey hostPubKey = hostKeyPair.getPublic();
			
			SHA2 sha2 = new SHA2();
			byte[] hostPseudoId = sha2.makeSHA2Hash(hostPubKey.getEncoded());
			// CalleeId is the string entered by the user
			byte[] calledPseudoIdByte = Base64.decodeBase64(calleeId); 

			// ================================================================
			// Get callee's information from DHT_GET
			// ================================================================
			Get getMessage = new Get(calledPseudoIdByte);
			dhtCommunicator.sendMessage(getMessage);
			lastReceivedMessage = dhtCommunicator.readIncomingAndHandleError();

			if (!dhtCommunicator.isValidMessage(lastReceivedMessage,
					GetReply.messageName, calledPseudoIdByte)) {
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
					TnReady.messageName, hostPseudoId)) {
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
						destinationIpv4.getHostAddress(), configParser);
			}
			else {
				System.out.println("Cannot connect");
			}

		} catch (IOException e) {
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
		}

		if (dhtCommunicator != null) {
			dhtCommunicator = null;
		}
		
		if (kxCommunicator != null) {
			// TODO: ask kx to destroy outgoing tunnel
			// kxCommunicator.
			kxCommunicator = null;
		}

	}
}
