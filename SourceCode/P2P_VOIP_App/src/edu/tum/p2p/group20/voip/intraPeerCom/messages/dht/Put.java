package edu.tum.p2p.group20.voip.intraPeerCom.messages.dht;

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
	
	public Put (byte[] key, short ttl, int replication, byte[] content) {
		super();
		byteValues.put("key", key);
		byteValues.put("ttl", Helper.networkOrderedBytesFromShort(ttl));
		byteValues.put("replication", new byte[] { (byte) replication });
		// confused on why an int is directly casted as byte without bothering about endianness?
		// 	read this -> http://stackoverflow.com/a/10756545/976880
		byteValues.put("reserved", new byte[5]);
		byteValues.put("content", content);		
	}
}
