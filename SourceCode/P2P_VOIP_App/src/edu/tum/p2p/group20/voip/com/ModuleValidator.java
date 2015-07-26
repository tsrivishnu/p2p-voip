package edu.tum.p2p.group20.voip.com;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.commons.codec.binary.Base64;

public class ModuleValidator {
	
	private MessageDigest messageDigest;
	private String moduleVerificationSalt = Config.MODULE_VERIFICATION_SALT;
	private SimpleDateFormat dateFormatter = new SimpleDateFormat(Config.DATE_FORMAT);
	
	private java.util.Date timestamp;
	public String timestampString;
	public String digest;
	
	public ModuleValidator() throws NoSuchAlgorithmException {
		messageDigest = MessageDigest.getInstance("SHA-256");
		timestamp = new java.util.Date();
		timestampString = dateFormatter.format(timestamp);
		messageDigest.update((timestampString + moduleVerificationSalt).getBytes());
		digest = Base64.encodeBase64String(messageDigest.digest());
	}
	
	public ModuleValidator(String timestampString, String verificationHash) throws ParseException, NoSuchAlgorithmException {
		messageDigest = MessageDigest.getInstance("SHA-256");
		this.timestampString = timestampString;
		timestamp = dateFormatter.parse(this.timestampString);
		digest = verificationHash;
	}
	
	public Boolean isValid() {
		messageDigest.update((timestampString + moduleVerificationSalt).getBytes());
		String expectedDigest = Base64.encodeBase64String(messageDigest.digest());
		return expectedDigest.equals(digest);
	}
}
