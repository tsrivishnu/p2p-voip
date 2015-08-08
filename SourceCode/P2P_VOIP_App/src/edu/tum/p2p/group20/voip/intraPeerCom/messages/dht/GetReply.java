package edu.tum.p2p.group20.voip.intraPeerCom.messages.dht;

import java.util.Arrays;

import edu.tum.p2p.group20.voip.intraPeerCom.messages.ReceivedMessage;

public class GetReply extends ReceivedMessage {
	static {
		messageName = "MSG_DHT_GET_REPLY";
		
		fields = new String[] {
	      "size",
	      "messageCode",
	      "pseudoId",
	      "content"
		};
	}
	
	public GetReply(byte[] fullMessageBytes) {
		byteValues.put("size", Arrays.copyOfRange(fullMessageBytes, 0, 2));
		byteValues.put("messageCode", Arrays.copyOfRange(fullMessageBytes, 2, 4));
		byteValues.put("pseudoId", Arrays.copyOfRange(fullMessageBytes, 4, 36));
		byteValues.put("content", Arrays.copyOfRange(fullMessageBytes, 36, fullMessageBytes.length));		
	}
}