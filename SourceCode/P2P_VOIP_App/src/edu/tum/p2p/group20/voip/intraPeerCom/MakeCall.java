package edu.tum.p2p.group20.voip.intraPeerCom;

import java.io.IOException;
import java.net.InetAddress;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;

import edu.tum.p2p.group20.voip.config.ConfigParser;
import edu.tum.p2p.group20.voip.crypto.RSA;
import edu.tum.p2p.group20.voip.crypto.SHA2;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.ReceivedMessage;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.dht.Get;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.dht.GetReply;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.dht.TraceReply;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.kx.BuildTNIncoming;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.kx.BuildTNOutgoing;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.kx.TnReady;
import edu.tum.p2p.group20.voip.voice.CallInitiatorListener;
import edu.tum.p2p.group20.voip.voice.Sender;

// TODO handle KX_TN_DESTROY when the process has to be killled
public class MakeCall {
	public static ReceivedMessage lastReceivedMessage;
	public static IntraPeerCommunicator dhtCommunicator;
	public static IntraPeerCommunicator kxCommunicator;
	private Sender sender;
	private CallInitiatorListener callInitiatorListener;
	private ConfigParser configParser;

	
	public void makeCall(String calleeId,ConfigParser configParser) throws Exception{
        this.configParser = configParser;
     
        
        try {
        	dhtCommunicator = new IntraPeerCommunicator(configParser.getDhtHost(), configParser.getDhtPort());
        	kxCommunicator = new IntraPeerCommunicator(configParser.getKxhost(), configParser.getKxPort());
        	
        	KeyPair hostKeyPair = RSA.getKeyPairFromFile(configParser.getHostKey());
        	PublicKey hostPubKey = hostKeyPair.getPublic();
  
        	SHA2 sha2 = new SHA2();
        	byte[] hostPseudoId = sha2.makeSHA2Hash(hostPubKey.getEncoded());
        	
        	byte[] calledPseudoIdByte = Base64.decodeBase64(calleeId); 
        	
        	Get getMessage = new Get(calledPseudoIdByte);
        	dhtCommunicator.sendMessage(getMessage);
        	
        	lastReceivedMessage = dhtCommunicator.readIncomingAndHandleError();        
        	
        	//checking reply for signature
        	if (!dhtCommunicator.isValidMessage(lastReceivedMessage, GetReply.messageName, calledPseudoIdByte)) {
        		throw new Exception("GET reply error");
        	} 
        	byte[] publickKeyBytes = lastReceivedMessage.get("publicKey");
        	RSAPublicKey remotePublicKey = RSA.getPublicKeyFromBytes(publickKeyBytes);
        	
        	//check for null pointer
        	byte[] xchangePointInfoForKx = lastReceivedMessage.get("xchangePointInfoForKx");
        	
        	
        	// Send request to KX to build tunnel
        	sendKxBuildOutgoingTunnel(hostPseudoId, xchangePointInfoForKx);
        	lastReceivedMessage = kxCommunicator.readIncomingAndHandleError();
        	
        	if (kxCommunicator.isValidMessage(lastReceivedMessage, TnReady.messageName, hostPseudoId)) {        		        		
        		//check for null pointer
        		InetAddress destinationIpv4 = InetAddress.getByAddress(lastReceivedMessage.get("ipv4"));
        		InetAddress destinationIpv6 = InetAddress.getByAddress(lastReceivedMessage.get("ipv6"));
        		System.out.println("Now connect to: " + destinationIpv4);
        		System.out.println("Now connect to: " + destinationIpv6);
        		
        		//create the object for sender module
        		sender = new Sender();
        		sender.setCallInitiatorListener(callInitiatorListener);
        		//TODO: check this id needs to be send or public key
        		sender.initiateCall(calleeId, remotePublicKey, destinationIpv4.getHostAddress(), configParser);
        		//initialize sender with destination point info
        		
        	} else {
        		System.out.println("Cannot connect");
        	}
        	
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
	}
	
	private  void sendKxBuildOutgoingTunnel(byte[] pseudoId, byte[] xchangePointInfo) throws IOException {
		BuildTNOutgoing buildTnMessage = new BuildTNOutgoing(3, pseudoId, xchangePointInfo);
		kxCommunicator.sendMessage(buildTnMessage);
	}

	public CallInitiatorListener getCallInitiatorListener() {
		return callInitiatorListener;
	}

	public void setCallInitiatorListener(CallInitiatorListener callInitiatorListener) {
		this.callInitiatorListener = callInitiatorListener;
	}
}
