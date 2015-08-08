package edu.tum.p2p.group20.voip.intraPeerCom.messages.dht;

import java.util.Arrays;

import edu.tum.p2p.group20.voip.intraPeerCom.messages.ReceivedMessage;

public class TraceReply extends ReceivedMessage {
	static {
		messageName = "MSG_DHT_GET_REPLY";
		
		fields = new String[] {
	      "size",
	      "messageCode",
	      "pseudoId",
	      "xchangePointInfo"
		};
	}
	
	public TraceReply(byte[] fullMessageBytes) {
		byteValues.put("size", Arrays.copyOfRange(fullMessageBytes, 0, 2));
		byteValues.put("messageCode", Arrays.copyOfRange(fullMessageBytes, 2, 4));
		byteValues.put("pseudoId", Arrays.copyOfRange(fullMessageBytes, 4, 36));
    	// read the trace message's last hops details because that will contain the
    	// information that we need to for the exchange point.
    	// i.e, look for the first 56 bytes in the content.
		byteValues.put("xchangePointInfo", Arrays.copyOfRange(fullMessageBytes, 36, 92));		
	}
}
