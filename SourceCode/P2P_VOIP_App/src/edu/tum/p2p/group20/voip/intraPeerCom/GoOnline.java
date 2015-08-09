package edu.tum.p2p.group20.voip.intraPeerCom;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.util.Scanner;

import edu.tum.p2p.group20.voip.crypto.RSA;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.ReceivedMessage;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.ReceivedMessageFactory;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.RequestMessage;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.dht.Get;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.dht.Put;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.dht.Trace;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.dht.TraceReply;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.kx.BuildTNIncoming;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.kx.TnReady;

public class GoOnline {
	
	private static Socket clientSocket;
	private static OutputStream out;
	private static InputStream in;
	public static ReceivedMessage lastReceivedMessage;
	
	public static void main(String[] args) throws Exception {

		if (args.length != 1) {
            System.err.println("Usage: java Sender <port number>");
            System.exit(1);
        }
         
        int portNumber = Integer.parseInt(args[0]);
        
        try {        	
        	clientSocket = new Socket("127.0.0.1", portNumber);
        	clientSocket.setSoTimeout(10000); // 10 Seconds timeout
    		out = clientSocket.getOutputStream();
        	in = clientSocket.getInputStream();
        		
            KeyPair hostKeyPair = RSA.getKeyPairFromFile("lib/receiver_private.pem");
        	String hostPseudoIdentity = "9caf4058012a33048ca50550e8e32285c86c8f3013091ff7ae8c5ea2519c860c";
            
        	MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        	messageDigest.update(hostPseudoIdentity.getBytes());        	
        	byte[] key = messageDigest.digest();
        	byte[] randomPsuedoId = null;
        	        	
        	// finding a exchange point.
        	boolean isRandomPseudoIdChosen = false;
        	// TODO what if you are never able to find a random non existing pseudo id? 
        	//      this loop continues for ever?
        	while (!isRandomPseudoIdChosen) {
	        	// Pick a random pseudo id
	        	randomPsuedoId = messageDigest.digest(new java.util.Date().toString().getBytes());
	        	// Do a DHT_GET to find if that id exists
	        	Get dhtGet = new Get(randomPsuedoId);
	        	System.out.println("Sending DHT_GET for randomID");
	        	sendMessage(dhtGet);
	        	readIncomingAndHandleError();
	    		// When either message is not received or message is not a valid reply,
	        	//  we have a random not existing pseudoId
	    		// 	If message is a valid reply, that means the pseudo id exists.
	    		if(lastReceivedMessage == null ||  !dhtGet.isValidReply(lastReceivedMessage)) {
	    			isRandomPseudoIdChosen = true;
	    			System.out.println("Found a random Pseudo ID");
	    		}
        	}
    		
        	byte[] xchangePointInfoFromTrace = doDhtTraceForRandomExchangePoint(randomPsuedoId);        	       
        	
        	byte[] xChangePointInfoForKx = Helper
        			.trasnformXChangePointInfoFromDhtToKx(xchangePointInfoFromTrace);        	
        	
        	// Send request to KX to build tunnel
        	sendKxBuildIncomingTunnel(key, xChangePointInfoForKx);
        	readIncomingAndHandleError();
        	
        	if (isValidMessage(lastReceivedMessage, TnReady.messageName, key)) {        		
        		
        		sendDhtPutMessage(key, hostKeyPair.getPublic().getEncoded(), xChangePointInfoForKx);
            	readIncomingAndHandleError(); // This is just to see if you would get any error
        		
        		System.out.println("You are now online!");
        	} else {
        		System.out.println("Offline!");
        	}
        } catch (IOException e) {
            System.out.println("Exception caught when trying to connect on port " + portNumber);
            System.out.println(e.getMessage());
        }
	}
		
	private static byte[] doDhtTraceForRandomExchangePoint(byte[] key) throws Exception {
		sendDhtTraceMessage(key);
		readIncomingAndHandleError();
		// if no reply is received, or received a wrong type raise exception
		if (!isValidMessage(lastReceivedMessage, TraceReply.messageName, key)) {
			throw new Exception("DHT trace reply error");
		}
		
		return lastReceivedMessage.get("xchangePointInfo");		
	}
	
	private static void sendDhtPutMessage(byte[] key, byte[] publicKey, byte[] xchangePointInfo) throws IOException {
		Put put_message = new Put(key, (short) 12, 255, publicKey, xchangePointInfo);
		sendMessage(put_message);				
	}
	
	private static void sendDhtTraceMessage(byte[] key) throws IOException {
		Trace traceMessage = new Trace(key);
		sendMessage(traceMessage);
	}
	
	public static void sendKxBuildIncomingTunnel(byte[] pseudoId, byte[] xchangePointInfo) throws IOException {
		BuildTNIncoming buildTnMessage = new BuildTNIncoming(3, pseudoId, xchangePointInfo);
		sendMessage(buildTnMessage);
	}
	
	private static void sendMessage(RequestMessage requestMessage) throws IOException {
		byte[] messageBytes = requestMessage.fullMessageAsBytes();
		System.out.println("Sending message: "  + requestMessage.messageName);
		out.write(messageBytes, 0, messageBytes.length);
	}
	
	private static void readIncomingAndHandleError() throws Exception {
		readIncomingMessage();
    	raiseExceptionIfError();
	}
	
	private static void raiseExceptionIfError() throws Exception {
		if ( lastReceivedMessage != null && lastReceivedMessage.isErrorType()) {
			throw new Exception("Error message Received: "+ lastReceivedMessage.name());
		}
	}
	
	private static ReceivedMessage readIncomingMessage() throws Exception {
		try {
			lastReceivedMessage = null;
			
			byte[] buff = new byte[2];		
	    	// First read the length
	        in.read(buff, 0, buff.length);
	        short incomingSize = Helper.shortFromNetworkOrderedBytes(buff);
	        
	        incomingSize = (short) (incomingSize - 2); // Cause two bytes are already read.
	        byte[] incomingBytes = new byte[incomingSize];
	        in.read(incomingBytes, 0, incomingSize);
	        
	        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
	        byteStream.write(buff);
	        byteStream.write(incomingBytes);
	        
	        lastReceivedMessage = ReceivedMessageFactory
	        		.getReceivedMessageFor(byteStream.toByteArray());
	        
	        System.out.println("Received message: " + lastReceivedMessage.name());
			
	        return lastReceivedMessage;
	        
		} catch(IOException e) {
			System.out.println("Exception caught while trying to read network message");
            System.out.println(e.getMessage());
            
            return null;
		}
	}
	
	private static boolean isValidMessage(ReceivedMessage receivedMessage, String expectedName, byte[] pseudoId) {
		return (receivedMessage != null 
    			&& receivedMessage.name().equals(expectedName)
    			&& receivedMessage.isValid(pseudoId));
	}
}
