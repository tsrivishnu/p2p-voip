package edu.tum.p2p.group20.voip.intraPeerCom.messages.dht;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Arrays;

import edu.tum.p2p.group20.voip.crypto.RSA;
import edu.tum.p2p.group20.voip.intraPeerCom.Helper;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.RequestMessage;

public class Put extends RequestMessage {
	
	static {
		messageName = "MSG_DHT_PUT";
		
		fields = new String[] {
	      "size",
	      "messageCode",
	      "pseudoId",
	      "ttl",
	      "replication",
	      "reserved",
	      "publicKey",
	      "pseudoIdToBeSigned",
	      "xchangePointInfoForKx",
	      "signature"
		};
	}
	
	
	// TODO Probably, take hostKeyPair, it can perform signing and etc., as needed.
	public Put (byte[] pseudoId, short ttl, int replication, KeyPair rsaKeyPair, byte[] xchangePointInfo) throws IOException, InvalidKeyException, SignatureException, NoSuchAlgorithmException {
		super();
		byteValues.put("pseudoId", pseudoId);
		byteValues.put("ttl", Helper.networkOrderedBytesFromShort(ttl));
		byteValues.put("replication", new byte[] { (byte) replication });
		// confused on why an int is directly casted as byte without bothering about endianness?
		// 	read this -> http://stackoverflow.com/a/10756545/976880
		byteValues.put("reserved", new byte[5]);
		byteValues.put("publicKey", rsaKeyPair.getPublic().getEncoded());
		byteValues.put("pseudoIdToBeSigned", pseudoId);
		byteValues.put("xchangePointInfoForKx", xchangePointInfo);
		
		ByteArrayOutputStream toBeSignedStream = new ByteArrayOutputStream();
		toBeSignedStream.write(byteValues.get("pseudoIdToBeSigned"));
		toBeSignedStream.write(byteValues.get("xchangePointInfoForKx"));
		
		byteValues.put("signature", RSA.sign(rsaKeyPair.getPrivate(), toBeSignedStream.toByteArray()));		
	}
}
