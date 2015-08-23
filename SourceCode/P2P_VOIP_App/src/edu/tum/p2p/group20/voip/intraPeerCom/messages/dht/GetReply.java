package edu.tum.p2p.group20.voip.intraPeerCom.messages.dht;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import edu.tum.p2p.group20.voip.crypto.RSA;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.ReceivedMessage;

public class GetReply extends ReceivedMessage {
	static {
		messageName = "MSG_DHT_GET_REPLY";
		
		fields = new String[] {
	      "size",
	      "messageCode",
	      "pseudoId",
	      "publicKey",
	      "pseudoIdToBeSigned",
	      "xchangePointInfoForKx",
	      "signature"
		};
	}
	
	public GetReply(byte[] fullMessageBytes) {
		byteValues.put("size", Arrays.copyOfRange(fullMessageBytes, 0, 2));
		byteValues.put("messageCode", Arrays.copyOfRange(fullMessageBytes, 2, 4));
		byteValues.put("pseudoId", Arrays.copyOfRange(fullMessageBytes, 4, 36));

		// TODO Find out if public key is always gonna be 550 bytes
		byteValues.put("publicKey", Arrays.copyOfRange(fullMessageBytes, 36, 586));
		byteValues.put("pseudoIdToBeSigned", Arrays.copyOfRange(fullMessageBytes, 586, 618)); // 32 bytes
		byteValues.put("xchangePointInfoForKx", Arrays.copyOfRange(fullMessageBytes, 618, 674)); // size 56
		byteValues.put("signature", Arrays.copyOfRange(fullMessageBytes, 674, fullMessageBytes.length));		
	}
	
	public boolean isValid(byte[] pseudoId) throws Exception {
		// Validate the signature
		if (!Arrays.equals(byteValues.get("pseudoId"), pseudoId)) {
			return false;
		}
		
		ByteArrayOutputStream toBeSignedStream = new ByteArrayOutputStream();
		toBeSignedStream.write(get("pseudoIdToBeSigned"));
		toBeSignedStream.write(get("xchangePointInfoForKx"));
		
		return RSA.verify(
			RSA.getPublicKeyFromBytes(get("publicKey")),
			toBeSignedStream.toByteArray(),
			get("signature")
		);
	}
}