package edu.tum.p2p.group20.voip.intraPeerCom.messages.dht;

public class Trace extends IntraPeerMessage {	
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
