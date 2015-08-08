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
		// TODO Auto-generated method stub
		
		byte[] size;
		String hostPseudoIdentity = "9caf4058012a33048ca50550e8e32285c86c8f3013091ff7ae8c5ea2519c860c";
        
    	MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
    	messageDigest.update(hostPseudoIdentity.getBytes());        	
    	byte[] key = messageDigest.digest();
    	
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
		
		ReceivedMessage receivedMessage = ReceivedMessageFactory.getReceivedMessageFor(fullDhtReplyMessage);
		
		System.out.println(Arrays.toString(peer1Id));
//		System.out.println(Arrays.toString(receivedMessage.byteValues.get("content")));
		
		byte[] xchangeInfoData =  Helper
    			.trasnformXChangePointInfoFromDhtToKx(
    					receivedMessage.byteValues.get("xchangePointInfo")
    			);
		System.out.println(Arrays.toString(Arrays.copyOfRange(xchangeInfoData, 4, 36)));
		System.out.println(
					Helper.shortFromNetworkOrderedBytes(
						Arrays.copyOfRange(xchangeInfoData, 0, 2)
					)
				);
		System.out.println(
				Helper.shortFromNetworkOrderedBytes(
					Arrays.copyOfRange(xchangeInfoData, 2, 4)
				)
			);
		
		System.out.println(InetAddress.getByAddress(Arrays.copyOfRange(xchangeInfoData, 36, 40)).getHostName());
		System.out.println(InetAddress.getByAddress(Arrays.copyOfRange(xchangeInfoData, 40, xchangeInfoData.length)).getHostName());
		System.out.println("******");
		
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
