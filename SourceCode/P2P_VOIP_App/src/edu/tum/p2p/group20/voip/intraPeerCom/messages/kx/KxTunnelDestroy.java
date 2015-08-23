/**
 * 
 */
package edu.tum.p2p.group20.voip.intraPeerCom.messages.kx;

import edu.tum.p2p.group20.voip.intraPeerCom.messages.RequestMessage;

public class KxTunnelDestroy extends RequestMessage{
	
	public String[] fields() {
		return new String[] {
		  "size",
	      "messageCode",
	      "pseudoId"
		};
	}
	
	public String messageName() {
		return "MSG_KX_TN_DESTROY";
	}
	
	public KxTunnelDestroy(byte[] pseudoId) {
		super();
		byteValues.put("pseudoId", pseudoId);
	}
}
