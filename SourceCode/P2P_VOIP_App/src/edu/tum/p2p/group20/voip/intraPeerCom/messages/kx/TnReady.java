package edu.tum.p2p.group20.voip.intraPeerCom.messages.kx;

import java.util.Arrays;

import edu.tum.p2p.group20.voip.intraPeerCom.messages.ReceivedMessage;

public class TnReady extends ReceivedMessage {
	
	public String[] fields() {
		return new String[] {
	      "size",
	      "messageCode",
	      "pseudoId",
	      "reserved",
	      "ipv4",
	      "ipv6"
		};
	}
	
	public String messageName() {
		return "MSG_KX_TN_READY";
	}
	
	public TnReady(byte[] fullMessageBytes) {
		byteValues.put("size", Arrays.copyOfRange(fullMessageBytes, 0, 2));
		byteValues.put("messageCode", Arrays.copyOfRange(fullMessageBytes, 2, 4));
		byteValues.put("pseudoId", Arrays.copyOfRange(fullMessageBytes, 4, 36));
		byteValues.put("reserved", Arrays.copyOfRange(fullMessageBytes, 36, 40));
		byteValues.put("ipv4", Arrays.copyOfRange(fullMessageBytes, 40, 44));
		byteValues.put("ipv6", Arrays.copyOfRange(fullMessageBytes, 44, fullMessageBytes.length));
	}
}
