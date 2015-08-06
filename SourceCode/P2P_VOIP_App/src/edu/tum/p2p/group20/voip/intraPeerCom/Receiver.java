package edu.tum.p2p.group20.voip.intraPeerCom;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import edu.tum.p2p.group20.voip.crypto.RSA;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.dht.Get;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.dht.Put;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.dht.Trace;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.kx.BuildTNIncoming;

public class Receiver {

	private static ServerSocket serverSocket;
	private static Socket clientSocket;
	private static OutputStream out;
	private static InputStream in;
	public static String lastReceivedMessageName;
	public static byte[] lastReceivedMessage;
	
	public static void main(String[] args) throws NumberFormatException, UnknownHostException, IOException, NoSuchAlgorithmException, InvalidKeySpecException {

		if (args.length != 1) {
            System.err.println("Usage: java Sender <port number>");
            System.exit(1);
        }
         
        int portNumber = Integer.parseInt(args[0]);
        
        try {
        	serverSocket = new ServerSocket(portNumber);        	
        	clientSocket = serverSocket.accept();
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
	        	sendMessageBytes(dhtGet.fullMessageAsBytes());
	    		readIncomingMessage();
	    		// When either message is not received or message is not a valid reply
	    		// 	If message is a valid reply, that means the pseudo id exists.
	    		if(lastReceivedMessage == null || !dhtGet.isValidReply(lastReceivedMessage)) {
	    			isRandomPseudoIdChosen = true;
	    			System.out.println("Found a random Pseudo ID");
	    		}
        	}
    		
        	byte[] xchangePointInfoFromTrace = doDhtTraceForRandomExchangePoint(randomPsuedoId);        	       
        	
        	byte[] xChangePointInfoForKx = Helper
        			.trasnformXChangePointInfoFromDhtToKx(xchangePointInfoFromTrace);
        	
        	sendDhtPutMessage(key, hostKeyPair.getPublic().getEncoded(), xChangePointInfoForKx);
        	
        	// Send request to KX to build tunnel
        	sendKxBuildIncomingTunnel(key, xChangePointInfoForKx);
        	// Handle error response from KX

        } catch (IOException e) {
            System.out.println("Exception caught when trying to connect on port " + portNumber);
            System.out.println(e.getMessage());
        }
	}
	
	public static byte[] doDhtTraceForRandomExchangePoint(byte[] key) throws IOException {
		sendDhtTraceMessage(key);
		readIncomingMessage();
    	// read the trace message's last hops details because that will contain the
    	// information that we need to for the exchange point.
    	// i.e, look for the last 56 bytes in the messages.
    	return Arrays.copyOfRange(lastReceivedMessage, lastReceivedMessage.length-56, lastReceivedMessage.length);
	}
	
	public static void sendDhtPutMessage(byte[] key, byte[] publicKey, byte[] xchangePointInfo) throws IOException {
		Put put_message = new Put(key, (short) 12, 255, publicKey, xchangePointInfo);
		sendMessageBytes(put_message.fullMessageAsBytes());				
	}
	
	public static void sendDhtTraceMessage(byte[] key) throws IOException {
		Trace traceMessage = new Trace(key);
    	byte[] traceMessageBytes = traceMessage.fullMessageAsBytes();
    	sendMessageBytes(traceMessageBytes);
	}
	
	private static void sendMessageBytes(byte[] messageBytes) throws IOException {
		out.write(messageBytes, 0, messageBytes.length);
	}
	
	public static void sendKxBuildIncomingTunnel(byte[] pseudoId, byte[] xchangePointInfo) throws IOException {
		BuildTNIncoming buildTnMessage = new BuildTNIncoming(3, pseudoId, xchangePointInfo);
		sendMessageBytes(buildTnMessage.fullMessageAsBytes());
	}
	
	private static byte[] readIncomingMessage() throws IOException {
		// TODO: Where ever you are calling this method. Handle the error response of 
		//  	that module. Or should we handle it here?
		try {
			lastReceivedMessageName = null;
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
	        
	        short messageCode = Helper.shortFromNetworkOrderedBytes(
				Arrays.copyOfRange(byteStream.toByteArray(), 2, 4)
			);        
	        
	        System.out.println("Received Message: " + MessagesLegend.nameForCode(messageCode));
	        
	        // Assign message detail to the static variables
	        lastReceivedMessageName = MessagesLegend.nameForCode(messageCode);
	        lastReceivedMessage = byteStream.toByteArray();
	        
	        return lastReceivedMessage;
	        
		} catch(IOException e) {
			System.out.println("Exception caught while trying to read network message");
            System.out.println(e.getMessage());
            
            return null;
		}
	}
}
