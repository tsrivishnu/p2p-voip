package edu.tum.p2p.group20.voip.intraPeerCom.messages.dht;

import java.util.Arrays;

public class Get extends IntraPeerMessage {	
	static {
		messageName = "MSG_DHT_GET";
		
		fields = new String[] {
	      "size",
	      "messageCode",
	      "key"
		};
	}
	
	public Get(byte[] pseudoIdToSearch) {
		super();		
		byteValues.put("key", pseudoIdToSearch);
	}
	
	/**
	 * Checks if the passed message bytes corresponds to the message's
	 * reply.
	 * 
	 * @param fullReplyMessage in byte[]
	 * @return true or false
	 */
	public boolean isValidReply(byte[] fullReplyMessage) {
		String messagCode = IntraPeerMessage.messageCodeFromFullMessage(fullReplyMessage); 
		if (!messagCode.equals("DHT_GET_REPLY")) {
			return false;
		}
		
		byte[] replyKey = Arrays.copyOfRange(fullReplyMessage, 4, fullReplyMessage.length);
		return Arrays.equals(byteValues.get("key"), replyKey);
	}
}
