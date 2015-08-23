package edu.tum.p2p.group20.voip.intraPeerCom.messages.dht;

import edu.tum.p2p.group20.voip.intraPeerCom.messages.ReceivedMessage;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.RequestMessage;

public class Get extends RequestMessage {	
	
	public String[] fields() {
		return new String[] {
	      "size",
	      "messageCode",
	      "pseudoId"
		};
	}
	
	public String messageName() {
		return "MSG_DHT_GET";
	}
	
	public Get(byte[] pseudoIdToSearch) {
		super();		
		byteValues.put("pseudoId", pseudoIdToSearch);
	}
	
	/**
	 * Checks if the passed message bytes corresponds to the message's
	 * reply.
	 * 
	 * @param fullReplyMessage in byte[]
	 * @return true or false
	 * @throws Exception 
	 */
	public boolean isValidReply(ReceivedMessage replyMessage) throws Exception {
		String messagCode = replyMessage.name(); 
		if (!messagCode.equals("MSG_DHT_GET_REPLY")) {
			return false;
		}
		
		return replyMessage.isValid(byteValues.get("pseudoId"));
	}
}
