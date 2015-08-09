package edu.tum.p2p.group20.voip.intraPeerCom.messages;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import edu.tum.p2p.group20.voip.intraPeerCom.Helper;
import edu.tum.p2p.group20.voip.intraPeerCom.MessagesLegend;

/**
 * Class that serves as parent class and handles basic functionalities of 
 * encapsulating required fields in a intra peer communication's received message into
 * the required format
 * 
 * The contructors of the subclasses usually take the received bytes as arguments and 
 * contruct the message.
 * 
 * @author Sri Vishnu Totakura <srivishnu@totakura.in>
 *
 */
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
	
	/**
	 * Matches the pseudId in the message to the Id passed as argument and return
	 * true or false.
	 * 
	 * @param pseudoId
	 * @return true/false boolean
	 */
	public boolean isValid(byte[] pseudoId) {
		return Arrays.equals(byteValues.get("pseudoId"), pseudoId);
	}
	
	public String name() {
		return messageName;
	}
	
	public byte[] get(String key) {
		return byteValues.get(key);				
	}
	
	public boolean isErrorType() {
		return false;
	}
}
