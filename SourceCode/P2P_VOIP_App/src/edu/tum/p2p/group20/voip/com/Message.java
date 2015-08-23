package edu.tum.p2p.group20.voip.com;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
	public JSONObject fullMessage;
	public Object signature;
	public Boolean isDecrypted = false;
	public Boolean isEncrypted = false;
	public MessageCrypto messageCrypto;
	private SimpleDateFormat dateFormatter = new SimpleDateFormat(Config.DATE_FORMAT);
	
	/**
	 * Create an instance of Message from a JSON string.
	 *  
	 * @param json
	 * @param encrypted: Boolean which says if the data in the message is encrypted or not.
	 * @param messageCrypto
	 * @throws ParseException
	 */	
	public Message(String json, Boolean encrypted, MessageCrypto messageCrypto) throws ParseException {
		
		fullMessage = (JSONObject) jsonParser.parse(json);
		isEncrypted = encrypted;
		if (isEncrypted){
			encryptedData = (String) fullMessage.get("message");
		} else {
			data = (JSONObject) fullMessage.get("message");
		}
		
		this.messageCrypto = messageCrypto;
		signature = fullMessage.get("signature");
	}
	
	/**
	 * Initializes an empty instance of Message to which data can be added. 
	 * 
	 */
	public Message(MessageCrypto messageCrypto) {
		data = new JSONObject();
		fullMessage = new JSONObject();
		fullMessage.put("message", data);
		this.messageCrypto = messageCrypto;
		addPseudoIdentities();
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
		return dateFormatter.parse((String) fullMessage.get("timestamp"));
	}
	
	/**
	 * Performs signing on the message's data and appends signature to the message root.
	 *  
	 */
	public void sign() {
		
		try {
			signature = messageCrypto.getSignature(
				toBeSignedJSONObject().toJSONString()
			);
			
			fullMessage.put("signature", signature);
		} catch (SignatureException e) {
			e.printStackTrace();
		}	
	}
	
	/**
	 * Validate the signature and the timestamp over the timestamp passed as argument.
	 * Validates of the message type is as expected
	 * Also, validates the sender and receiver of the message.
	 * 
	 * If the timestamp is before the timestamp passed, it is considered invalid.
	 * If the sender and receiver are not as expected, it is considered invalid.
	 * 
	 * @param timestamp
	 * @param messageType
	 * @return true or false
	 */
	public boolean isValid(Date prevTimestamp, String messageType) {
		
		String signature = (String) fullMessage.get("signature");		
		
		try {
			if (!messageCrypto.isValidSignature(signature, toBeSignedJSONObject().toJSONString())){			
				System.out.println("Signature fail");
				return false;
			}
			
			if (!prevTimestamp.before(timestamp())) {
				System.out.println("Timestamp fail");
				// Message is valid only if the prevTimestamp is before this message's timestamp.
				return false;
			}
			
			// At this stage, we could decrypt and		
			// Match if sender and receiver of the message are as expected 
			if (isEncrypted) {
				decrypt();
			}
			
			if (messageType !=null && !messageType.equals(data.get("type"))) {
				return false;
			}
			
		} catch (InvalidKeyException | SignatureException
			| NoSuchAlgorithmException | UnsupportedEncodingException
			| ShortBufferException | IllegalBlockSizeException
			| BadPaddingException | ParseException | java.text.ParseException e) {
			
			e.printStackTrace();
			return false;
		}
		
		if ( !((data.get("sender").equals(messageCrypto.otherPartyPseudoIdentity))
				&&
			   (data.get("receiver").equals(messageCrypto.hostPseudoIdentity)))) {
			System.out.println("Entities fail");
			return false;
		}
		
		return true;
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
	public void encrypt() {
		
		byte[] encryptedBytes;
		try {
			encryptedBytes = messageCrypto.encryptWithSessionKey(data.toJSONString());
			encryptedData = Base64.encodeBase64String(encryptedBytes);
			fullMessage.put("message", encryptedData);
			isEncrypted = true;
		} catch (InvalidKeyException | ShortBufferException
				| IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}
		
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
		if (!isDecrypted) {
			byte[] encryptedBytes = Base64.decodeBase64(encryptedData);
			data = (JSONObject) jsonParser.parse(messageCrypto.decryptWithSessionKey(encryptedBytes));
			fullMessage.put("message", data);
			isDecrypted = true;
		} 
	}
	
	/**
	 * Adds a timestamp to the message in its root and performs signing and returns it 
	 * as a string to be used to send in the network
	 * 
	 * @return Message with a timestamp and signing as a string.
	 */
	public String asJSONStringForExchange() {
		
		return asJSONStringForExchange(true, true);
	}
	
	/**
	 * Returns message as a string to be used to send in the network
	 * 
	 * @param addTimestamp: To append timestamp to the message root
	 * @param addSignature: To sign the message and append signature to the message root.
	 * @return Message as a string
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws SignatureException
	 * @throws UnsupportedEncodingException
	 */
	public String asJSONStringForExchange(boolean addTimestamp, boolean addSignature){
		if (addTimestamp) {
			addTimestamp();
		}
		
		if (addSignature) {
			sign();
		}
		
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
		fullMessage.put("timestamp", dateFormatter.format(new Date()));
	}
	
	private void addPseudoIdentities() {
		put("sender", messageCrypto.hostPseudoIdentity);
		put("receiver", messageCrypto.otherPartyPseudoIdentity);
	}
	
	/**
	 * Returns the part of the full message which has to be signed.
	 * 
	 * @return JSONObject which has to be signed.
	 */
	private JSONObject toBeSignedJSONObject() {
		JSONObject toBeSigned = new JSONObject();
		if (isEncrypted) {
			toBeSigned.put("message", encryptedData);
		} else {
			toBeSigned.put("message", data);
		}
		
		toBeSigned.put("timestamp", fullMessage.get("timestamp"));
		return toBeSigned;
	}
}
