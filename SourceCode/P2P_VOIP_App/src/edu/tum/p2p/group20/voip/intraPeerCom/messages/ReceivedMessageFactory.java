package edu.tum.p2p.group20.voip.intraPeerCom.messages;

import java.util.Arrays;

import edu.tum.p2p.group20.voip.intraPeerCom.Helper;
import edu.tum.p2p.group20.voip.intraPeerCom.MessagesLegend;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.dht.DhtError;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.dht.GetReply;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.dht.TraceReply;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.kx.KxError;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.kx.TnReady;

public class ReceivedMessageFactory {
	
	public static ReceivedMessage getReceivedMessageFor(byte[] fullMessageBytes) {
		short messageCode = Helper.shortFromNetworkOrderedBytes(
			Arrays.copyOfRange(fullMessageBytes, 2, 4)
		);
		
		ReceivedMessage receivedMessage;
		
		switch (MessagesLegend.nameForCode(messageCode)) {
			case "MSG_DHT_GET_REPLY":
				receivedMessage = new GetReply(fullMessageBytes);
				break;
			case "MSG_DHT_TRACE_REPLY":
				receivedMessage = new TraceReply(fullMessageBytes);
				break;
			case "MSG_DHT_ERROR":
				receivedMessage = new DhtError(fullMessageBytes);
				break;
			case "MSG_KX_ERROR":
				receivedMessage = new KxError(fullMessageBytes);
				break;
			case "MSG_KX_TN_READY":
				receivedMessage = new TnReady(fullMessageBytes);
				break;	
			default:
				receivedMessage = new ReceivedMessage();
				break;
		}
		
		return receivedMessage; 
	}
}