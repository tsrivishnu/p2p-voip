package edu.tum.p2p.group20.voip.intraPeerCom;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

/**
 * Class to maintain a record and provide API for various message type names
 * and their correspoding codes used during communication with other modules
 * of the peer.
 * 
 * @author Sri Vishnu Totakura <srivishnu@totakura.in>
 *
 */
public class MessagesLegend {
	public static final Map<String, Short> legend;
	static
    {
		legend = new HashMap<String, Short>();
		legend.put("MSG_DHT_PUT", (short) 500);
		legend.put("MSG_DHT_GET", (short) 501);
		legend.put("MSG_DHT_TRACE", (short) 502);
		legend.put("MSG_DHT_GET_REPLY", (short) 503);
		legend.put("MSG_DHT_TRACE_REPLY", (short) 504);
		legend.put("MSG_DHT_ERROR", (short) 505);
		
		legend.put("MSG_KX_TN_BUILD_IN", (short) 600);
		legend.put("MSG_KX_TN_BUILD_OUT", (short) 601);
		legend.put("MSG_KX_TN_READY", (short) 602);
		legend.put("MSG_KX_TN_DESTROY", (short) 603);
		legend.put("MSG_KX_ERROR", (short) 604);		
    }	
	
	/**
	 * Returns name of the message from its code
	 * 
	 * @param messageCode
	 * @return null or message name
	 */
	public static String nameForCode(short messageCode) {
	    for (Entry<String, Short> entry : legend.entrySet()) {
	        if (Objects.equals(messageCode, entry.getValue())) {
	            return entry.getKey();
	        }
	    }
	    return null;
	}
	
	/**
	 * Returns code for the given message name
	 * 
	 * @param messageName
	 * @return null or code for the message name
	 */
	public static short codeForName(String messageName) {
		return legend.get(messageName);
	}
}
