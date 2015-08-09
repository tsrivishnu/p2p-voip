package edu.tum.p2p.group20.voip.intraPeerCom.messages.dht;

import edu.tum.p2p.group20.voip.intraPeerCom.messages.RequestMessage;

public class Trace extends RequestMessage {	
	static {
		messageName = "MSG_DHT_TRACE";
		
		fields = new String[] {
	      "size",
	      "messageCode",
	      "key"
		};
	}
	
	public Trace(byte[] key) {
		super();		
		byteValues.put("key", key);
	}
}
