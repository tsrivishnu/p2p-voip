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

/**
 * MessageCryto: Handles encryption, decryption, signing and signature verification of given strings 
 * from messages.
 * Instances of this class are usually set as delegates for crypto functions in Message class.
 * 
 * @author Sri Vishnu Totakura <srivishnu@totakura.in>
 *
 */
public class MessageCrypto {
	private SecretKeySpec keySpec;
    private Cipher cipher;
    private byte[] sessionKey;
    
	/**
	 * Creates a new instance of MessageCrypto with given session key
	 * 
	 * @param sessionKey
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws NoSuchPaddingException
	 */
	public MessageCrypto(byte[] sessionKey) throws NoSuchAlgorithmException,
				NoSuchProviderException, NoSuchPaddingException {
		
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		
		this.sessionKey = sessionKey;
		keySpec = new SecretKeySpec(sessionKey, "AES");
		cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
	}
	
	/**
	 * Encrypts a given string using the session key passed during initialization
	 * 
	 * @param toEncrypt
	 * @return Cipher text as a byte array
	 * @throws InvalidKeyException
	 * @throws ShortBufferException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public byte[] encrypt(String toEncrypt) throws InvalidKeyException,
					ShortBufferException, IllegalBlockSizeException,
					BadPaddingException {
		
		cipher.init(Cipher.ENCRYPT_MODE, keySpec);
		
        byte[] toEncryptBytes = toEncrypt.getBytes();

        byte[] cipherText = new byte[cipher.getOutputSize(toEncryptBytes.length)];
        int ctLength = cipher.update(toEncryptBytes, 0, toEncryptBytes.length, cipherText, 0);
        ctLength += cipher.doFinal(cipherText, ctLength);
        
        return cipherText;
	}
	
	/**
	 * Decrypts a given cipher text using the session key provided during initialization 
	 * and return plain text as a string.
	 * 
	 * @param cipherText
	 * @return Plain text for the corresponding cipher text as string
	 * @throws InvalidKeyException
	 * @throws ShortBufferException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public String decrypt(byte[] cipherText) throws InvalidKeyException,
					ShortBufferException, IllegalBlockSizeException,
					BadPaddingException {
		
		cipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] plainText = new byte[cipher.getOutputSize(cipherText.length)];
        int ptLength = cipher.update(cipherText, 0, cipherText.length, plainText, 0);
        ptLength += cipher.doFinal(plainText, ptLength);
        
        return new String(plainText).trim(); // trim because the length of the string from byte array is wrong.
	}
}
