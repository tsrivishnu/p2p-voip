package edu.tum.p2p.group20.voip.intraPeerCom;

import java.util.HashMap;
import java.util.Map;

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
	
}
