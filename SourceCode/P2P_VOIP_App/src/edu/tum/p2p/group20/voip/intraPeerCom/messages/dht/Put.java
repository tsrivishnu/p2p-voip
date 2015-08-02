package edu.tum.p2p.group20.voip.intraPeerCom.messages.dht;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;

import edu.tum.p2p.group20.voip.intraPeerCom.Helper;

public class Put {
	public byte[] key;
	public byte[] ttl;
	public byte[] messageCode = Helper.networkOrderedBytesFromShort((short) 500);
	public byte[] reserved = new byte[3];
	public byte replication;
	public byte[] content;
	public byte[] message;
	
	public Put (byte[] key, short ttl, int replication, byte[] content) {
		this.key = key;
		this.ttl = Helper.networkOrderedBytesFromShort(ttl);
		this.replication = (byte) replication;
		this.content = content;
	}
	
	public Put (byte[] messageBytes) {
		short size = Helper.shortFromNetworkOrderedBytes(
				Arrays.copyOfRange(messageBytes, 0, 2)
			);
		short messageCode1 = Helper.shortFromNetworkOrderedBytes(
					Arrays.copyOfRange(messageBytes, 2, 4)
				);
		byte[] key = Arrays.copyOfRange(messageBytes, 4, 36);
		content = Arrays.copyOfRange(messageBytes, 42, 592);
		System.out.println("*******************");
//        System.out.println(Arrays.toString(Arrays.copyOfRange(messageBytes, 2, 4)));
//        System.out.println(Arrays.toString(messageCode));        
        System.out.println(messageCode1);
//        System.out.println(Arrays.toString(key));
        System.out.println(Base64.encodeBase64String(key));
        System.out.println(Arrays.toString(content));
	}
	
	public short sizeAsShort() {
		short size = 0;
		size = (short) (size + 2); // for size field.
		size = (short) (size + messageCode.length);
		size = (short) (size + key.length);
		size = (short) (size + ttl.length);
		size = (short) (size + 1); // for replication
		size = (short) (size + 3); // For reserved
		size = (short) (size + content.length);
		
		return size;
	}
	
	public byte[] fullMessage() throws IOException {
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(sizeAsBytes());
		outputStream.write(messageCode);
		outputStream.write(key);
		outputStream.write(ttl);		
		outputStream.write(replication);
		outputStream.write(reserved);
		outputStream.write(content);

		message = outputStream.toByteArray();
		
		return message;
	}
	
	private byte[] sizeAsBytes() {
		return Helper.networkOrderedBytesFromShort(sizeAsShort());
	}
}
