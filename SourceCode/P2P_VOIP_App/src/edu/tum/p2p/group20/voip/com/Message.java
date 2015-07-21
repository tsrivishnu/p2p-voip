package edu.tum.p2p.group20.voip.com;

import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.ShortBufferException;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Message responsible for generating the messages in required format for the 
 * inter-modular communication. Hanldes generation of JSON messages, encrypting the data,
 * adding signature and verifying signature.
 * 
 * @author Sri Vishnu Totakura <srivishnu@totakura.in>
 *
 */
public class Message {
	private static JSONParser jsonParser = new JSONParser();
	public JSONObject data;
	public String encryptedData;
	private JSONObject fullMessage;
	public Object signature;
	public MessageCrypto messageCrypto;
	
	/**
	 * Create an instance of Message from a JSON string.
	 *  
	 * @param json
	 * @param encrypted: Boolean which says if the data in the message is encrypted or not.
	 * @throws ParseException
	 */	
	public Message(String json, Boolean encrypted) throws ParseException {
		fullMessage = (JSONObject) jsonParser.parse(json);
		
		if (encrypted){
			encryptedData = (String) fullMessage.get("message");
		} else {
			data = (JSONObject) fullMessage.get("message");
		}
		
		signature = fullMessage.get("signature");
	}
	
	/**
	 * Initializes an empty instance of Message to which data can be added. 
	 * 
	 */
	public Message() {
		data = new JSONObject();
		fullMessage = new JSONObject();
		fullMessage.put("message", data);
	}
	
	/**
	 * Add key value pair to the json of the message.
	 *  
	 * @param key
	 * @param value
	 */
	public void put(Object key, Object value) {
		data.put(key, value);
	}
	
	/**
	 * Get the value of the key from the JSON of the message. 
	 * 
	 * @param key
	 * @return value assosciated for the key passed.
	 */
	public Object get(String key) {
		return data.get(key);
	}
	
	/**
	 * Get the whole message encapsulated into the format as a JSON string
	 * 
	 * @return JSON String of the whole formatted message.
	 */
	public String asJSON() {
		return fullMessage.toJSONString();
	}
	
	/**
	 * Performs signing on the message's data and encapsulates it into the required format.
	 */
	public void sign() {
		fullMessage.put("signature", "Some signature goes here");
	}
	
	/**
	 * Encrypts the message's data and encapsulate it into the required format.
	 * 
	 * Note: After calling this method, the data in the message is replaced with the
	 * encrypted data. So, trying to get a value or setting a value would not work as expected.
	 * 
	 * @throws InvalidKeyException
	 * @throws ShortBufferException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public void encrypt() throws InvalidKeyException, ShortBufferException,
					IllegalBlockSizeException, BadPaddingException {
		
		byte[] encryptedBytes = messageCrypto.encrypt(data.toJSONString());
		encryptedData = Base64.encodeBase64String(encryptedBytes);
		fullMessage.put("message", encryptedData);
	}
	
	/**
	 * Decrypts the message's data part and encapsulates into the required format.
	 * 
	 * @throws InvalidKeyException
	 * @throws ShortBufferException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws ParseException
	 */
	public void decrypt() throws InvalidKeyException, ShortBufferException,
					IllegalBlockSizeException, BadPaddingException, ParseException {
		byte[] encryptedBytes = Base64.decodeBase64(encryptedData);
		data = (JSONObject) jsonParser.parse(messageCrypto.decrypt(encryptedBytes));
		fullMessage.put("message", data);
	}
}
