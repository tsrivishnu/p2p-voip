package edu.tum.p2p.group20.voip.com;

import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.ShortBufferException;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import edu.tum.p2p.group20.voip.crypto.RSA;

public class MessageUnitTest {

	KeyPair senderHostKeyPair;
	KeyPair receiverHostKeyPair;
	
	PublicKey receiverPublicKey;
	PublicKey senderPublicKey;
	
	String receiverPseudoIdentity;
	String senderPseudoIdentity;
	
	MessageCrypto messageCryptoForSender;
	MessageCrypto messageCryptoForReceiver;
	
	SimpleDateFormat sdf = Config.DATE_FORMATTER;	
	
	Message sentMessage;
	Message receivedMessage; 
	
	@Test
	public void isValidTest() throws IOException, GeneralSecurityException, ParseException, java.text.ParseException {
		
		senderHostKeyPair = RSA.getKeyPairFromFile("/Users/Munna/Desktop/sender_private.pem");
		receiverHostKeyPair = RSA.getKeyPairFromFile("/Users/Munna/Desktop/receiver_private.pem");
		
		receiverPublicKey = RSA.getPublicKey("/Users/Munna/Desktop/receiver.pub");
		senderPublicKey = RSA.getPublicKey("/Users/Munna/Desktop/sender.pub");
		
		receiverPseudoIdentity = "dc429ac06ffec501db88cbed0c5c685d82542c927f0fb3e28b4845be16156dea";
		senderPseudoIdentity = "9caf4058012a33048ca50550e8e32285c86c8f3013091ff7ae8c5ea2519c860c";
		
		messageCryptoForSender = new MessageCrypto(senderHostKeyPair, receiverPublicKey, senderPseudoIdentity, receiverPseudoIdentity);
		messageCryptoForReceiver = new MessageCrypto(receiverHostKeyPair, senderPublicKey, receiverPseudoIdentity, senderPseudoIdentity);
		
		messageCryptoForSender.setSessionKey("[B@46f5f77912343".getBytes());
		messageCryptoForReceiver.setSessionKey("[B@46f5f77912343".getBytes());
		
		sentMessage = new Message(messageCryptoForSender);
		
		sentMessage.put("key1", "Value1");
		sentMessage.put("key2", "Value2");
		sentMessage.encrypt();
//		System.out.println(sentMessage.asJSONStringForExchange());			
		
		receivedMessage = new Message(sentMessage.asJSONStringForExchange(), true, messageCryptoForReceiver);
		
		testSignatureRelated();
		testTimestampRelated();
		testPseudoIdentitiesRelated();
	}
	
	private void testSignatureRelated() throws InvalidKeyException, SignatureException, NoSuchAlgorithmException, UnsupportedEncodingException, java.text.ParseException, ShortBufferException, IllegalBlockSizeException, BadPaddingException, ParseException {

		String originalMessage = receivedMessage.encryptedData;		
		// When data is tampered with
		receivedMessage.encryptedData = new StringBuilder(originalMessage).reverse().toString();
		// should return false
		assertFalse(receivedMessage.isValid(sdf.parse("2015-07-25 15:39:28.705")));		
		
		// Put back the original message;
		receivedMessage.encryptedData = (String) originalMessage;
		
		assertTrue(receivedMessage.isValid(sdf.parse("2015-07-25 15:39:28.705")));
	}
	
	private void testTimestampRelated() throws InvalidKeyException, SignatureException, NoSuchAlgorithmException, UnsupportedEncodingException, java.text.ParseException, ShortBufferException, IllegalBlockSizeException, BadPaddingException, ParseException {
		// Passing a past timestamp should return true
		assertTrue(receivedMessage.isValid(sdf.parse("2015-07-25 15:39:28.705")));
		
		//Passing the same timestamp should return false
		assertFalse(receivedMessage.isValid(receivedMessage.timestamp()));
		
		// Passing a later timestamp should return false
		assertFalse(receivedMessage.isValid(new java.util.Date()));
	}
	
	public void testPseudoIdentitiesRelated() throws InvalidKeyException, SignatureException, NoSuchAlgorithmException, UnsupportedEncodingException, java.text.ParseException, ShortBufferException, IllegalBlockSizeException, BadPaddingException, ParseException {		
		Object senderOriginal = receivedMessage.data.get("sender");
		Object receiverOriginal = receivedMessage.data.get("receiver");
		
		assertTrue(receivedMessage.isValid(sdf.parse("2015-07-25 15:39:28.705")));
		
//		change the pseudoIdentity of sender in the message.
		receivedMessage.data.put("sender", "sdafasdfasd");
		assertFalse(receivedMessage.isValid(sdf.parse("2015-07-25 15:39:28.705")));
		receivedMessage.data.put("sender", senderOriginal);		
		
//		change the pseudoIdentity of receiver in the message.
		receivedMessage.data.put("receiver", "sdafasdfasd");
		assertFalse(receivedMessage.isValid(sdf.parse("2015-07-25 15:39:28.705")));
		receivedMessage.data.put("receiver", receiverOriginal);		
		
		
		
		
	}

}
