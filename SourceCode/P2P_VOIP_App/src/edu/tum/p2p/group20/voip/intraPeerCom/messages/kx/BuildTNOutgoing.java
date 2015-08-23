package edu.tum.p2p.group20.voip.intraPeerCom.messages.kx;

import edu.tum.p2p.group20.voip.intraPeerCom.messages.RequestMessage;

public class BuildTNOutgoing extends RequestMessage {

	public String[] fields() {
		return new String[] {
		  "size",
		  "messageCode",
		  "totalHops",
		  "reserved",
		  "pseudoId",
		  "xchangePointInfo"
		};
	}
	
	public String messageName() {
		return "MSG_KX_TN_BUILD_OUT";
	}
	
	public BuildTNOutgoing(int totalHops, byte[] pseudoId, byte[] xchangePointInfo) {
		super();
		byteValues.put("totalHops", new byte[] { (byte) totalHops });
		// confused on why an int is directly casted as byte without bothering about endianness?
		// 	read this -> http://stackoverflow.com/a/10756545/976880
		byteValues.put("reserved", new byte[3]);
		byteValues.put("pseudoId", pseudoId);
		byteValues.put("xchangePointInfo", xchangePointInfo);
	}
}
