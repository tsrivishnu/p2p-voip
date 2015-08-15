package edu.tum.p2p.group20.voip.intraPeerCom;

import java.io.IOException;
import java.net.InetAddress;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.util.Arrays;

import edu.tum.p2p.group20.voip.crypto.RSA;
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
	public static IntraPeerCommunicator communicator;
	private Sender sender;
	private CallInitiatorListener callInitiatorListener;
	public static void main(String[] args) throws Exception {
		MakeCall mkCall = new MakeCall();
		if (args.length != 1) {
            System.err.println("Usage: java Sender <port number>");
            System.exit(1);
        }
         
        int portNumber = Integer.parseInt(args[0]);
        mkCall.makeCall(portNumber,"somerandomPseudoId");
        
	}
	
	public void makeCall(int portNumber,String calleeId) throws Exception{
        
     
        
        try {
        	communicator = new IntraPeerCommunicator("127.0.0.1", portNumber);
        	
        	KeyPair hostKeyPair = RSA.getKeyPairFromFile("lib/receiver_private.pem");
        	String hostPseudoIdentity = "9caf4058012a33048ca50550e8e32285c86c8f3013091ff7ae8c5ea2519c860c";
            
        	
        	
        	MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        	
        	byte[] hostPseudoId = messageDigest.digest(hostPseudoIdentity.getBytes());
        	
//        	messageDigest.update("somerandomthing".getBytes());        	
//        	byte[] pseudoIdToSearch = messageDigest.digest();
        	
        	Get getMessage = new Get(calleeId.getBytes());
        	communicator.sendMessage(getMessage);
        	
        	lastReceivedMessage = communicator.readIncomingAndHandleError();        
        	
        	// TODO The getReply should validate the message content with its signature.
        	if (!communicator.isValidMessage(lastReceivedMessage, GetReply.messageName, calleeId.getBytes())) {
        		throw new Exception("GET reply error");
        	}
        	//check for null pointer
        	byte[] xchangePointInfoForKx = lastReceivedMessage.get("xchangePointInfoForKx");
        	
        	
        	// Send request to KX to build tunnel
        	sendKxBuildOutgoingTunnel(hostPseudoId, xchangePointInfoForKx);
        	lastReceivedMessage = communicator.readIncomingAndHandleError();
        	
        	if (communicator.isValidMessage(lastReceivedMessage, TnReady.messageName, hostPseudoId)) {        		        		
        		//check for null pointer
        		InetAddress destinationIpv4 = InetAddress.getByAddress(lastReceivedMessage.get("ipv4"));
        		InetAddress destinationIpv6 = InetAddress.getByAddress(lastReceivedMessage.get("ipv6"));
        		System.out.println("Now connect to: " + destinationIpv4.toString());
        		System.out.println("Now connect to: " + destinationIpv6.toString());
        		
        		//create the object for sender module
        		sender = new Sender();
        		sender.setCallInitiatorListener(callInitiatorListener);
        		//initialize sender with destination point info
        		
        	} else {
        		System.out.println("Cannot connect");
        	}
        	
        } catch (IOException e) {
            System.out.println("Exception caught when trying to connect on port " + portNumber);
            System.out.println(e.getMessage());
        }
	}
	
	public static void sendKxBuildOutgoingTunnel(byte[] pseudoId, byte[] xchangePointInfo) throws IOException {
		BuildTNOutgoing buildTnMessage = new BuildTNOutgoing(3, pseudoId, xchangePointInfo);
		communicator.sendMessage(buildTnMessage);
	}

	public CallInitiatorListener getCallInitiatorListener() {
		return callInitiatorListener;
	}

	public void setCallInitiatorListener(CallInitiatorListener callInitiatorListener) {
		this.callInitiatorListener = callInitiatorListener;
	}
}
