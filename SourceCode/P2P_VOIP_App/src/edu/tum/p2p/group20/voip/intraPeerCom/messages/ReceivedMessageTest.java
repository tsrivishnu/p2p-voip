package edu.tum.p2p.group20.voip.intraPeerCom.messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import edu.tum.p2p.group20.voip.intraPeerCom.Helper;
import edu.tum.p2p.group20.voip.intraPeerCom.MessagesLegend;




public class ReceivedMessageTest {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		byte[] size;
		String hostPseudoIdentity = "9caf4058012a33048ca50550e8e32285c86c8f3013091ff7ae8c5ea2519c860c";
        
    	MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
    	messageDigest.update(hostPseudoIdentity.getBytes());        	
    	byte[] key = messageDigest.digest();
    	
		byte[] messageCode = Helper.networkOrderedBytesFromShort(
				(short) MessagesLegend.codeForName("MSG_DHT_GET_REPLY")
			);
		byte[] content = "Dummycontent".getBytes();		
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();		
		outputStream.write(messageCode);
		outputStream.write(key);
		outputStream.write(content);		
		
		byte[] fullDhtReplyMessage = prependSizeForMessage(outputStream.toByteArray());
		
		ReceivedMessage receivedMessage = ReceivedMessageFactory.getReceivedMessageFor(fullDhtReplyMessage);
		
		System.out.println(Arrays.toString(content));
		System.out.println(Arrays.toString(receivedMessage.byteValues.get("content")));
		System.out.println(receivedMessage.isValid(new byte[32]));
	}

	private static byte[] prependSizeForMessage(byte[] message) throws IOException {
		byte[] size = Helper.networkOrderedBytesFromShort((short) (message.length + 2)); //+2 for size of size field
		ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream();
		outputStream2.write(size);
		outputStream2.write(message);
		byte[] fullMessage = outputStream2.toByteArray();
		return fullMessage;		
	}
}
