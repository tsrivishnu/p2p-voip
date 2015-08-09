package edu.tum.p2p.group20.voip.intraPeerCom;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Scanner;

import edu.tum.p2p.group20.voip.crypto.RSA;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.dht.Put;

public class KXSimulator {
	
	private static ServerSocket serverSocket;
	public static Socket socket;
	
	public static OutputStream out;
	public static InputStream in;
	public static Scanner userIn;
	public static String lastReceivedMessageName;
	public static byte[] lastReceivedMessage;

	public static void main(String[] args) throws Exception {

		if (args.length != 1) {
            System.err.println("Usage: java Sender <port number>");
            System.exit(1);
        }
         
        int portNumber = Integer.parseInt(args[0]);
        
        try {
        	serverSocket = new ServerSocket(portNumber);        	
        	socket = serverSocket.accept();
	        out = socket.getOutputStream();
        	in = socket.getInputStream();
        	userIn = new Scanner(System.in); 
        	
        	byte[] receivedMessage;
        	
        	receiveMessagesLoop: while(readIncomingMessage() != null) {
        		String userMessage = "What do you want to do?";
        		userMessage += "\n1.Receive next message";
        		userMessage += "\n2.Send DHT_TRACE_REPLY";
        		userMessage += "\n3.Send Dummy DHT_GET_REPLY";
        		userMessage += "\n4.Send IN Tunnel Ready";
        		userMessage += "\n5.Send OUT Tunnel Ready";
        		userMessage += "\n6.Send DHT_ERROR";
        		userMessage += "\n7.Send KX_ERROR";
        		userMessage += "\n8.Send nothing";
        		userMessage += "\n9.BREAK";
        		userMessage += "\n10.Send reply with wrong pseudo ID";
                System.out.println(userMessage);
                switch (userIn.nextLine()) {
				case "2":
					sendDhtTraceReply();
					break;
				case "3":
					sendDhtDummyGetReply();
					break;
				case "4":
					sendTunnelReady(new byte[4], new byte[16]);
					break;
				case "5":			
					sendTunnelReady(
						InetAddress.getByName("192.168.2.2").getAddress(),
						InetAddress.getByName("3ffe:2a00:100:7031::1").getAddress()
					);
					break;
				case "6":
					sendErrorReply("MSG_DHT_ERROR");
					break;
				case "7":
					sendErrorReply("MSG_KX_ERROR");
					break;
				case "9":
					break receiveMessagesLoop;
				default:
					break;
				}
        	}
        	
        	
            byte[] key = Arrays.copyOfRange(lastReceivedMessage, 2, 4);            

        } catch (IOException e) {
            System.out.println("Exception caught when trying to connect on port " + portNumber);
            System.out.println(e.getMessage());
        }
	}
	
	private static byte[] readIncomingMessage() throws IOException {
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
        lastReceivedMessageName = MessagesLegend.nameForCode(messageCode);
        lastReceivedMessage = byteStream.toByteArray();
        
        return lastReceivedMessage;
	}
	
	/**
	 * Send a Dummy DHT GET REPLY for the received DHT GET message.
	 * This is only a Dummy, the content in this makes no sense.
	 * 
	 * @throws IOException
	 * @throws NoSuchAlgorithmException 
	 * @throws SignatureException 
	 * @throws InvalidKeyException 
	 */
	private static void sendDhtDummyGetReply() throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {

		byte[] pseudoId = Arrays.copyOfRange(lastReceivedMessage, 4, 36);
		byte[] messageCode = Helper.networkOrderedBytesFromShort(
				(short) MessagesLegend.codeForName("MSG_DHT_GET_REPLY")
			);
		
		// xChangePointInfo
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update("exchangePoint2".getBytes());
		byte[] peer2Id = md.digest();
		byte[] peer2KxPort = Helper.networkOrderedBytesFromShort((short) 3001);
		byte[] peer2reserved = new byte[2];
		byte[] peer2ip = InetAddress.getByName("192.168.2.2").getAddress();
		byte[] peer2ipv6 = InetAddress.getByName("3ffe:2a00:100:7031::1").getAddress();
		
		ByteArrayOutputStream xchangeStream = new ByteArrayOutputStream();
		xchangeStream.write(peer2KxPort);
		xchangeStream.write(peer2reserved);
		xchangeStream.write(peer2Id);
		xchangeStream.write(peer2ip);
		xchangeStream.write(peer2ipv6);
		byte[] xchangePointInfo = xchangeStream.toByteArray();
		
		KeyPair hostKeyPair = RSA.getKeyPairFromFile("lib/receiver_private.pem");
		
		// We use a sample put to get all the signature and proper content encapsulation
		Put samplePut = new Put(pseudoId,(short) 1000, 2, hostKeyPair, xchangePointInfo);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(messageCode);
		outputStream.write(pseudoId);
		//content
		outputStream.write(samplePut.byteValues.get("publicKey"));
		outputStream.write(samplePut.byteValues.get("pseudoIdToBeSigned"));
		outputStream.write(samplePut.byteValues.get("xchangePointInfoForKx"));
		outputStream.write(samplePut.byteValues.get("signature"));
		
		byte[] fullDhtReplyMessage = prependSizeForMessage(outputStream.toByteArray());		
		out.write(fullDhtReplyMessage, 0, fullDhtReplyMessage.length);
	}
	
	/**
	 * Prepends the size field for a message after calculating the size of the message and
	 * returns the full byte array of the full encapsulated message
	 * 
	 * @param message
	 * @return full message with size as byte array
	 * @throws IOException
	 */
	private static byte[] prependSizeForMessage(byte[] message) throws IOException {
		byte[] size = Helper.networkOrderedBytesFromShort((short) (message.length + 2)); //+2 for size of size field
		ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream();
		outputStream2.write(size);
		outputStream2.write(message);
		byte[] fullMessage = outputStream2.toByteArray();
		return fullMessage;		
	}
	
	/**
	 * Send a Sample DHT TRACE REPLY message based on the last message received
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	private static void sendDhtTraceReply() throws NoSuchAlgorithmException, IOException {
		byte[] size;
		byte[] key = Arrays.copyOfRange(lastReceivedMessage, 4, 36);
		byte[] messageCode = Helper.networkOrderedBytesFromShort(
				(short) MessagesLegend.codeForName("MSG_DHT_TRACE_REPLY")
			);
		
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update("exchangePoint1".getBytes());
		byte[] peer1Id = md.digest();
		byte[] peer1KxPort = Helper.networkOrderedBytesFromShort((short) 3001);
		byte[] peer1reserved = new byte[2];
		byte[] peer1ip = InetAddress.getByName("192.168.2.1").getAddress();
		byte[] peer1ipv6 = InetAddress.getByName("FEDC:BA98:7654:3210:FEDC:BA98:7654:3210").getAddress();
		
		md.update("exchangePoint2".getBytes());
		byte[] peer2Id = md.digest();
		byte[] peer2KxPort = Helper.networkOrderedBytesFromShort((short) 3001);
		byte[] peer2reserved = new byte[2];
		byte[] peer2ip = InetAddress.getByName("192.168.2.2").getAddress();
		byte[] peer2ipv6 = InetAddress.getByName("3ffe:2a00:100:7031::1").getAddress();
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(messageCode);
		outputStream.write(key);
		
		outputStream.write(peer2Id);
		outputStream.write(peer2KxPort);
		outputStream.write(peer2reserved);
		outputStream.write(peer2ip);
		outputStream.write(peer2ipv6);
		
		outputStream.write(peer1Id);
		outputStream.write(peer1KxPort);
		outputStream.write(peer1reserved);
		outputStream.write(peer1ip);
		outputStream.write(peer1ipv6);
		
		byte[] fullDhtReplyMessage = prependSizeForMessage(outputStream.toByteArray());		
		out.write(fullDhtReplyMessage, 0, fullDhtReplyMessage.length);
	}
	
	/**
	 * Sends a tunnel ready reply to the requester with the give destinationIpAddress value
	 * 
	 * @param destinationIpAddress
	 * @throws IOException
	 */
	private static void sendTunnelReady(byte[] destinationIpv4, byte[] destinationIpv6) throws IOException {		
		byte[] key = Arrays.copyOfRange(lastReceivedMessage, 8, 40);
		
		byte[] messageCode = Helper.networkOrderedBytesFromShort(
				(short) MessagesLegend.codeForName("MSG_KX_TN_READY")
			);
		byte[] reserved = new byte[4];
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(messageCode);
		outputStream.write(key);
		outputStream.write(reserved);
		outputStream.write(destinationIpv4);
		outputStream.write(destinationIpv6);
		
		byte[] fullTunnelReadyMessage = prependSizeForMessage(outputStream.toByteArray());		
		out.write(fullTunnelReadyMessage, 0, fullTunnelReadyMessage.length);
	}
	
	private static void sendErrorReply(String messageName) throws Exception {
		byte[] key = Arrays.copyOfRange(lastReceivedMessage, 4, 36);
		
		byte[] messageCode = Helper.networkOrderedBytesFromShort(
				(short) MessagesLegend.codeForName(messageName)
			);
		byte[] reserved = new byte[2];
		byte[] request_type = Helper.networkOrderedBytesFromShort((short) 10);
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(messageCode);
		outputStream.write(request_type);
		outputStream.write(reserved);
		outputStream.write(key);
		
		byte[] fullDhtErrorMessage = prependSizeForMessage(outputStream.toByteArray());
		out.write(fullDhtErrorMessage, 0, fullDhtErrorMessage.length);
	}
}
