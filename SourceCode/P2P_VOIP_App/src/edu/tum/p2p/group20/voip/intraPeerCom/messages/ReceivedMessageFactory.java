package edu.tum.p2p.group20.voip.intraPeerCom.messages;

import java.util.Arrays;

import edu.tum.p2p.group20.voip.intraPeerCom.Helper;
import edu.tum.p2p.group20.voip.intraPeerCom.MessagesLegend;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.dht.GetReply;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.dht.TraceReply;

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
			default:
				receivedMessage = new ReceivedMessage();
				break;
		}
		
		return receivedMessage; 
	}
}