package edu.tum.p2p.group20.voip.com;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class MessageTest {

	public static void main(String[] args) throws ParseException, NoSuchAlgorithmException,
				NoSuchProviderException, NoSuchPaddingException, InvalidKeyException,
				ShortBufferException, IllegalBlockSizeException, BadPaddingException {
		
		MessageCrypto messageCrypto = new MessageCrypto("[B@46f5f77912343".getBytes());
		
		Message message11 = new Message();
		message11.put("key1", "Value1");
		message11.put("key2", "Value2");
		System.out.println(message11.asJSON());	
		
		message11.messageCrypto = messageCrypto;
	
		message11.encrypt();
		message11.sign();
		
		System.out.println(message11.asJSON());
		
		Message message2 = new Message(message11.asJSON());
		message2.messageCrypto = messageCrypto;
		message2.decrypt();
		
		System.out.println(message2.get("key1"));
		System.out.println(message2.asJSON());
	}

}
