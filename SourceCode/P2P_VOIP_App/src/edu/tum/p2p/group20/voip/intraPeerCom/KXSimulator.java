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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Scanner;

public class KXSimulator {
	
	public static Socket socket;
	
	public static OutputStream out;
	public static InputStream in;
	public static Scanner userIn;
	public static String lastReceivedMessageName;
	public static byte[] lastReceivedMessage;

	public static void main(String[] args) throws NumberFormatException, UnknownHostException, IOException, NoSuchAlgorithmException {

		if (args.length != 1) {
            System.err.println("Usage: java Sender <port number>");
            System.exit(1);
        }
         
        int portNumber = Integer.parseInt(args[0]);
        
        try {
        	socket = new Socket("127.0.0.1", portNumber);
        	
    		out = socket.getOutputStream();
        	in = socket.getInputStream();
        	userIn = new Scanner(System.in);   
        	byte[] receivedMessage;
        	
        	receiveMessagesLoop: while(readIncomingMessage() != null) {
        		String userMessage = "What do you want to do?";
        		userMessage += "\n1.Receive next message";
        		userMessage += "\n2.Send DHT_TRACE_REPLY";
        		userMessage += "\n3.Send Dummy DHT_GET_REPLY";
        		userMessage += "\n4.Send DHT_ERROR";
        		userMessage += "\n5.Send nothing";
        		userMessage += "\n6.BREAK";
                System.out.println(userMessage);
                switch (userIn.nextLine()) {
				case "2":
					sendDhtTraceReply();
					break;
				case "3":
					sendDhtDummyGetReply();
					break;
				case "6":
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
	 */
	private static void sendDhtDummyGetReply() throws IOException {
		byte[] size;
		byte[] key = Arrays.copyOfRange(lastReceivedMessage, 4, 36);
		byte[] messageCode = Helper.networkOrderedBytesFromShort(
				(short) MessagesLegend.codeForName("MSG_DHT_GET_REPLY")
			);
		byte[] content = "Dummycontent".getBytes();		
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();		
		outputStream.write(messageCode);
		outputStream.write(key);
		outputStream.write(content);
		
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
		outputStream.write(peer1Id);
		outputStream.write(peer1KxPort);
		outputStream.write(peer1reserved);
		outputStream.write(peer1ip);
		outputStream.write(peer1ipv6);
		
		outputStream.write(peer2Id);
		outputStream.write(peer2KxPort);
		outputStream.write(peer2reserved);
		outputStream.write(peer2ip);
		outputStream.write(peer2ipv6);
		
		byte[] fullDhtReplyMessage = prependSizeForMessage(outputStream.toByteArray());		
		out.write(fullDhtReplyMessage, 0, fullDhtReplyMessage.length);
	}

}
