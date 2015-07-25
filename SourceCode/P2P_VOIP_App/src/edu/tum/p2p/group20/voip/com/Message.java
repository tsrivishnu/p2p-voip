package edu.tum.p2p.group20.voip.com;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.text.SimpleDateFormat;

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
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
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
	 * Returns the timestamp of the message
	 * 
	 * @return Timestamp of the message as java.util.Date
	 * @throws java.text.ParseException
	 */
	public java.util.Date timestamp() throws java.text.ParseException {
		return Config.DATE_FORMATTER.parse((String) fullMessage.get("timestamp"));
	}
	
	/**
	 * Performs signing on the message's data and encapsulates it into the required format.
	 * @throws UnsupportedEncodingException 
	 * @throws SignatureException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 */
	public void sign() throws InvalidKeyException, NoSuchAlgorithmException,
				SignatureException, UnsupportedEncodingException {
		
		signature = messageCrypto.getSignature(toBeSignedJSONObject().toJSONString());		
		fullMessage.put("signature", signature);
	}
	
	/**
	 * Validate the signature and the timestamp over the timestamp passed as argument.
	 * 
	 * 
	 * If the timestamp is before the timestamp passed, it is considered invalid.
	 * 
	 * @param timestamp
	 * @return true or false
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 * @throws java.text.ParseException 
	 */
	public Boolean isValid(java.util.Date prevTimestamp) throws InvalidKeyException,
					SignatureException, NoSuchAlgorithmException, UnsupportedEncodingException,
					java.text.ParseException {
		
		String signature = (String) fullMessage.get("signature");
		System.out.println(prevTimestamp);
		System.out.println(timestamp());
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		
		if(messageCrypto.isValidSignature(signature, toBeSignedJSONObject().toJSONString())){
			return prevTimestamp.before(timestamp());
		} else {
			return false;
		}
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
		
		byte[] encryptedBytes = messageCrypto.encryptWithSessionKey(data.toJSONString());
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
		data = (JSONObject) jsonParser.parse(messageCrypto.decryptWithSessionKey(encryptedBytes));
		fullMessage.put("message", data);
	}
	
	/**
	 * Adds a timestamp to the message in its root and performs signing and returns it 
	 * as a string to be used to send in the network
	 * 
	 * @return Message with a timestamp and signing as a string.
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws SignatureException
	 * @throws UnsupportedEncodingException
	 */
	public String asJSONStringForExchange() throws InvalidKeyException,
					NoSuchAlgorithmException, SignatureException, UnsupportedEncodingException {

		addTimestamp();
		sign();
		return asJSONString();
	}
	
	/**
	 * Get the whole message encapsulated into the format as a JSON string
	 * 
	 * This will not add timestamp and signature
	 * 
	 * @return JSON String of the whole formatted message.
	 */
	public String asJSONString() {
		return fullMessage.toJSONString();
	}
	
	/**
	 * Adds timestamp to the mesasge, not in the data json but to the root of the message json
	 * because the receiver will validate the signature before decrypting.
	 * 
	 */
	private void addTimestamp() {
		fullMessage.put("timestamp", dateFormatter.format(new java.util.Date()));
	}
	
	/**
	 * Returns the part of the full message which has to be signed.
	 * 
	 * @return JSONObject which has to be signed.
	 */
	private JSONObject toBeSignedJSONObject() {
		JSONObject toBeSigned = new JSONObject();
		toBeSigned.put("message", fullMessage.get("message"));
		toBeSigned.put("timestamp", fullMessage.get("timestamp"));
		return toBeSigned;
	}
}
