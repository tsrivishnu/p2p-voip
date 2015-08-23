package edu.tum.p2p.group20.voip.intraPeerCom.messages.kx;

import java.util.Arrays;

import edu.tum.p2p.group20.voip.intraPeerCom.messages.ReceivedMessage;

public class KxError extends ReceivedMessage {
	
	public String[] fields() {
		return new String[] {
	      "size",
	      "messageCode",
	      "request_type",
	      "reserved",
	      "psuedoId"
		};
	}
	
	public String messageName() {
		return "MSG_KX_ERROR";
	}
	
	public KxError(byte[] fullMessageBytes) {
		byteValues.put("size", Arrays.copyOfRange(fullMessageBytes, 0, 2));
		byteValues.put("messageCode", Arrays.copyOfRange(fullMessageBytes, 2, 4));
		byteValues.put("request_type", Arrays.copyOfRange(fullMessageBytes, 4, 6));
		byteValues.put("reserved", Arrays.copyOfRange(fullMessageBytes, 6, 8));
		byteValues.put("pseudoId", Arrays.copyOfRange(fullMessageBytes, 8, fullMessageBytes.length));    	
	}
	
	public boolean isErrorType() {
		return true;
	}
}
