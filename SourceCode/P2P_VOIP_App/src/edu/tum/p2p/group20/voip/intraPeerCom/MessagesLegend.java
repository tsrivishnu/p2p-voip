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
	public static final Map<String, Integer> legend;
	static
    {
		legend = new HashMap<String, Integer>();
		legend.put("MSG_DHT_PUT", 500);
		legend.put("MSG_DHT_GET", 501);
		legend.put("MSG_DHT_TRACE", 502);
		legend.put("MSG_DHT_GET_REPLY", 503);
		legend.put("MSG_DHT_TRACE_REPLY", 504);
		legend.put("MSG_DHT_ERROR", 505);
		
		legend.put("MSG_KX_TN_BUILD_IN", 600);
		legend.put("MSG_KX_TN_BUILD_OUT", 601);
		legend.put("MSG_KX_TN_READY", 602);
		legend.put("MSG_KX_TN_DESTROY", 603);
		legend.put("MSG_KX_ERROR", 604);		
    }	
	
	/**
	 * Returns name of the message from its code
	 * 
	 * @param messageCode
	 * @return null or message name
	 */
	public static String nameForCode(Integer messageCode) {
	    for (Entry<String, Integer> entry : legend.entrySet()) {
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
	public static Integer codeForName(String messageName) {
		return legend.get(messageName);
	}
}
