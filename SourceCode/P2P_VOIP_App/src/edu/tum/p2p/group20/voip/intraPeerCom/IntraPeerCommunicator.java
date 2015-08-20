package edu.tum.p2p.group20.voip.intraPeerCom;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import edu.tum.p2p.group20.voip.intraPeerCom.messages.ReceivedMessage;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.ReceivedMessageFactory;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.RequestMessage;

public class IntraPeerCommunicator {
	
	public Socket clientSocket;
	public OutputStream out;
	public InputStream in;
	
	public IntraPeerCommunicator(String ip, int portNumber) throws UnknownHostException, IOException {
		clientSocket = new Socket(ip, portNumber);
    	clientSocket.setSoTimeout(20000); // 10 Seconds timeout
		out = clientSocket.getOutputStream();
    	in = clientSocket.getInputStream();
	}
	
	public void run() throws Exception {
		// Override this!
	}
	
	public void sendMessage(RequestMessage requestMessage) throws IOException {
		byte[] messageBytes = requestMessage.fullMessageAsBytes();
		System.out.println("Sending message: "  + requestMessage.messageName);
		out.write(messageBytes, 0, messageBytes.length);
	}
	
	public ReceivedMessage readIncomingAndHandleError() throws Exception {
		ReceivedMessage receivedMessage = readIncomingMessage();
    	raiseExceptionIfError(receivedMessage);
    	return receivedMessage;
	}
	
	private void raiseExceptionIfError(ReceivedMessage lastReceivedMessage) throws Exception {
		if ( lastReceivedMessage != null && lastReceivedMessage.isErrorType()) {
			throw new Exception("Error message Received: "+ lastReceivedMessage.name());
		}
	}
	
	private ReceivedMessage readIncomingMessage() throws Exception {
		try {			
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
	        
	        ReceivedMessage lastReceivedMessage = ReceivedMessageFactory
	        		.getReceivedMessageFor(byteStream.toByteArray());
	        
	        System.out.println("Received message: " + lastReceivedMessage.name());
			
	        return lastReceivedMessage;
	        
		} catch(IOException e) {
			System.out.println("Exception caught while trying to read network message");
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
		}
	}
	
	protected boolean isValidMessage(ReceivedMessage receivedMessage, String expectedName, byte[] pseudoId) throws Exception {
		return (receivedMessage != null 
    			&& receivedMessage.name().equals(expectedName)
    			&& receivedMessage.isValid(pseudoId));
	}
}
