package edu.tum.p2p.group20.voip.intraPeerCom.messages.dht;

import edu.tum.p2p.group20.voip.intraPeerCom.messages.RequestMessage;

public class Trace extends RequestMessage {	
	
	public String[] fields() {
		return new String[] {
			"size",
			"messageCode",
			"key"
		};
	}
	
	public String messageName() {
		return "MSG_DHT_TRACE";
	}
	
	public Trace(byte[] key) {
		super();		
		byteValues.put("key", key);
	}
}
