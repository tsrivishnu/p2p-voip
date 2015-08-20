package edu.tum.p2p.group20.voip.crypto;

/**
 * Class used to encrypt the string
 */
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;



/**
 * AES encryption class for secure communication of messages. 
 *
 */
public class AES {
	private Cipher mAESCipher;
	private SecretKey mKey;
	private String mKeyString;

	public AES(String key) {

		if(mKey==null){
					setSecretKey(key);;
				}


		try {
			mAESCipher = Cipher.getInstance("AES");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		}

	}
	private void setSecretKey(String key) {
		mKeyString = key;
		byte[] keyByte;
		try {
			keyByte = key.getBytes("UTF-8");
			MessageDigest md5 = null;
			try {
				md5 = MessageDigest.getInstance("MD5");
				keyByte = md5.digest(keyByte);
				keyByte = Arrays.copyOf(keyByte, 16); // use only first 128 bit
				mKey = new SecretKeySpec(keyByte, "AES");
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
	}


	public String getSecretKey() {
		return mKeyString;
	}


	public String encrypt(String msg) {
		try {
			mAESCipher.init(Cipher.ENCRYPT_MODE, mKey);
			byte[] encrypted = mAESCipher.doFinal(msg.getBytes());
			return Base64.encodeBase64String(encrypted);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} 
		return null;
	}


	public String decrypt(String encryptedMsg) {
		try {
			mAESCipher.init(Cipher.DECRYPT_MODE, mKey);
			byte[] decrypted = mAESCipher.doFinal(Base64.decodeBase64(encryptedMsg));
			return new String(decrypted);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return null;
	}
}
