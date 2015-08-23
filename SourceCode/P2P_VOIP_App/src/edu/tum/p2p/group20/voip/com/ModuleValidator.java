package edu.tum.p2p.group20.voip.com;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.commons.codec.binary.Base64;

/**
 * Class responsible for validating the same module running on another 
 * computer or instance.
 * 
 * It generates verification hash or verifies a verification hash send by another module.
 * 
 * @author Sri Vishnu Totakura <srivishnu@totakura.in>
 *
 */
public class ModuleValidator {
	
	private MessageDigest messageDigest;
	private String moduleVerificationSalt = Config.MODULE_VERIFICATION_SALT;
	private SimpleDateFormat dateFormatter = new SimpleDateFormat(Config.DATE_FORMAT);
	
	private java.util.Date timestamp;
	public String timestampString;
	public String digest;
	
	/**
	 * Creates an instance of module validator with new timestamp and generates
	 * a verification hash for it.
	 * 
	 * @throws NoSuchAlgorithmException
	 */
	public ModuleValidator() {
		try {
			messageDigest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			
			e.printStackTrace();
		}
		timestamp = new java.util.Date();
		timestampString = dateFormatter.format(timestamp);
		messageDigest.update((timestampString + moduleVerificationSalt).getBytes());
		digest = Base64.encodeBase64String(messageDigest.digest());
	}
	
	/**
	 * Creates an instance of module validator from the timestamp string and 
	 * verification hash
	 * Usually used to create instance from verification data received from other 
	 * computer or instance.
	 * 
	 * @param timestampString
	 * @param verificationHash
	 * @throws ParseException
	 * @throws NoSuchAlgorithmException
	 */
	public ModuleValidator(String timestampString, String verificationHash) throws ParseException, NoSuchAlgorithmException {
		messageDigest = MessageDigest.getInstance("SHA-256");
		this.timestampString = timestampString;
		timestamp = dateFormatter.parse(this.timestampString);
		digest = verificationHash;
	}
	
	/**
	 * Returns true or false based of whether the verfication hash is for the 
	 * timestamp that was given during the initialization.
	 * 
	 * @return True or false stating whether the verificationHash is valid or not.
	 */
	public Boolean isValid() {
		messageDigest.update((timestampString + moduleVerificationSalt).getBytes());
		String expectedDigest = Base64.encodeBase64String(messageDigest.digest());
		return expectedDigest.equals(digest);
	}
}
