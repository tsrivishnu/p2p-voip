/**
 * 
 */
package edu.tum.p2p.group20.voip.intraPeerCom.messages.kx;

import edu.tum.p2p.group20.voip.intraPeerCom.messages.RequestMessage;

/**
 * @author anshulvij
 *
 */
public class KxTunnelDestroy extends RequestMessage{
	
	static {
		messageName = "MSG KX TN DESTROY";
		
		fields = new String[] {
	      "size",
	      "messageCode",
	      "pseudoId",
		};
	}
}
