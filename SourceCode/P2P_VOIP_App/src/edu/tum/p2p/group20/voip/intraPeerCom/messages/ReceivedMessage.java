package edu.tum.p2p.group20.voip.intraPeerCom.messages;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ReceivedMessage {
	public static String fields[] = {
      "size",
      "messageCode",
      "pseudoId"
	};
	
	public static String messageName = ""; 
	
	public Map<String, byte[]> byteValues = new HashMap<String, byte[]>();
	
	public ReceivedMessage() {
		
	}
	
	public boolean isValid(byte[] pseudoId) {
		return Arrays.equals(byteValues.get("pseudoId"), pseudoId);
	}	
}
