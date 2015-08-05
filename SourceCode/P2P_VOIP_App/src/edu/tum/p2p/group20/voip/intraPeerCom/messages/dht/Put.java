package edu.tum.p2p.group20.voip.intraPeerCom.messages.dht;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;

import edu.tum.p2p.group20.voip.intraPeerCom.Helper;

public class Put extends IntraPeerMessage {
	
	static {
		messageName = "MSG_DHT_PUT";
		
		fields = new String[] {
	      "size",
	      "messageCode",
	      "key",
	      "ttl",
	      "replication",
	      "reserved",
	      "content"
		};
	}
	
	
	// TODO Probably, take hostKeyPair, it can perform signing and etc., as needed.
	public Put (byte[] key, short ttl, int replication, byte[] publicKey, byte[] xchangePointInfo) throws IOException {
		super();
		byteValues.put("key", key);
		byteValues.put("ttl", Helper.networkOrderedBytesFromShort(ttl));
		byteValues.put("replication", new byte[] { (byte) replication });
		// confused on why an int is directly casted as byte without bothering about endianness?
		// 	read this -> http://stackoverflow.com/a/10756545/976880
		byteValues.put("reserved", new byte[5]);
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(publicKey);
		outputStream.write(xchangePointInfo);
		
		byteValues.put("content", outputStream.toByteArray());
	}
}
