package edu.tum.p2p.group20.voip.com;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.sql.Date;
import java.text.SimpleDateFormat;

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

import edu.tum.p2p.group20.voip.crypto.RSA;


public class MessageTest {

	public static void main(String[] args) throws ParseException, IOException,
							GeneralSecurityException, java.text.ParseException {
		
		KeyPair hostKeyPair = RSA.getKeyPairFromFile("/Users/Munna/Desktop/sender_private.pem");        	
    	PublicKey otherPartyPublicKey = RSA.getPublicKey("/Users/Munna/Desktop/receiver.pub");
    	
		MessageCrypto messageCrypto = new MessageCrypto(hostKeyPair, otherPartyPublicKey);
		messageCrypto.setSessionKey("[B@46f5f77912343".getBytes());
		
		
		String signature = messageCrypto.getSignature("This is me!");
		System.out.println(signature);
		
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
//		
//		java.util.Date parsed = sdf.parse("2015-07-25 15:39:28.703");
//		java.util.Date parsed2 = sdf.parse("2015-07-25 15:39:28.705");
//		System.out.println(parsed2.after(parsed));
//		
		Message message11 = new Message();
		message11.put("key1", "Value1");
		message11.put("key2", "Value2");		
		message11.messageCrypto = messageCrypto;
		System.out.println(message11.asJSONStringForExchange());			
		
		Message message2 = new Message(message11.asJSONStringForExchange(), true);
		message2.messageCrypto = messageCrypto;
		message2.decrypt();
		
		System.out.println(message2.get("key1"));
		System.out.println(message2.asJSONStringForExchange());
	}

}
