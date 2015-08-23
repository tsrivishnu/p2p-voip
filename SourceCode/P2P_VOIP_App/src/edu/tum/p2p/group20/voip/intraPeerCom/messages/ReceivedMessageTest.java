package edu.tum.p2p.group20.voip.intraPeerCom.messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import edu.tum.p2p.group20.voip.intraPeerCom.Helper;
import edu.tum.p2p.group20.voip.intraPeerCom.MessagesLegend;




public class ReceivedMessageTest {

	public static void main(String[] args) throws Exception {
		
		byte[] size;
		String hostPseudoIdentity = "9caf4058012a33048ca50550e8e32285c86c8f3013091ff7ae8c5ea2519c860c";
        
    	MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
    	messageDigest.update(hostPseudoIdentity.getBytes());        	
    	byte[] key = messageDigest.digest();
    	
		byte[] messageCode =  new byte[2];
		byte[] reserved = new byte[4];
		byte[] ipv4 = InetAddress.getByName("192.168.2.2").getAddress();
		byte[] ipv6 = InetAddress.getByName("3ffe:2a00:100:7031::1").getAddress();
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(messageCode);		
		outputStream.write(key);		
		outputStream.write(reserved);
		outputStream.write(ipv4);
		outputStream.write(ipv6);
		
		byte[] fullTnReadyMessage = prependSizeForMessage(outputStream.toByteArray());
		
		ReceivedMessage receivedMessage = ReceivedMessageFactory.getReceivedMessageFor(fullTnReadyMessage);
		
		System.out.println(InetAddress.getByAddress(receivedMessage.byteValues.get("ipv4")).toString());
		System.out.println(InetAddress.getByAddress(receivedMessage.byteValues.get("ipv6")).toString());
		System.out.println(Arrays.toString(receivedMessage.byteValues.get("ipv4")));
		System.out.println(Arrays.toString(ipv4));
		
		System.out.println(receivedMessage.isValid(key));
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
