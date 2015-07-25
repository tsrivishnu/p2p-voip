package edu.tum.p2p.group20.voip.com;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.security.SignatureException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;

import edu.tum.p2p.group20.voip.crypto.RSA;

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
    private KeyPair hostKeyPair;
    private PublicKey otherPartyPublicKey;
    
    
	/**
	 * Creates a new instance of MessageCrypto with given RSA host key pair and otherparty's
	 * RSA public key
	 * 
	 * @param hostKeyPair
	 */	
	public MessageCrypto(KeyPair hostKeyPair, PublicKey otherPartyPublicKey) {
		
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		
		this.hostKeyPair = hostKeyPair;
		this.otherPartyPublicKey = otherPartyPublicKey;		
	}
	
	/**
	 * Sets the sessionKey for further encryption and decryption.
	 * 
	 * @param sessionKey
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchProviderException 
	 * @throws NoSuchAlgorithmException 
	 */
	public void setSessionKey(byte[] sessionKey) throws NoSuchAlgorithmException,
					NoSuchProviderException, NoSuchPaddingException {
		this.sessionKey = sessionKey;
		keySpec = new SecretKeySpec(sessionKey, "AES");
		cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
	}
	
	/**
	 * Encrypts a given string using the session key
	 * 
	 * @param toEncrypt
	 * @return Cipher text as a byte array
	 * @throws InvalidKeyException
	 * @throws ShortBufferException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public byte[] encryptWithSessionKey(String toEncrypt) throws InvalidKeyException,
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
	 * Decrypts a given cipher text using the session key and return plain text as a string.
	 * 
	 * @param cipherText
	 * @return Plain text for the corresponding cipher text as string
	 * @throws InvalidKeyException
	 * @throws ShortBufferException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public String decryptWithSessionKey(byte[] cipherText) throws InvalidKeyException,
					ShortBufferException, IllegalBlockSizeException,
					BadPaddingException {
		
		cipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] plainText = new byte[cipher.getOutputSize(cipherText.length)];
        int ptLength = cipher.update(cipherText, 0, cipherText.length, plainText, 0);
        ptLength += cipher.doFinal(plainText, ptLength);
        
        return new String(plainText).trim(); // trim because the length of the string from byte array is wrong.
	}
	
	/**
	 * Generates a signature for the given string using the host key provided during initialization
	 * 
	 * @param toBeSignedString: String that has to be signed.
	 * @return Signature for the string passed.
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws SignatureException
	 * @throws UnsupportedEncodingException
	 */
	public String getSignature(String toBeSignedString) throws InvalidKeyException,
					NoSuchAlgorithmException, SignatureException,
					UnsupportedEncodingException {
		
		return RSA.sign(hostKeyPair.getPrivate(), toBeSignedString);
	}
	
	/**
	 * Verifies the given signature against the given string using the other party's public key
	 * provided during initialization
	 * 
	 * @param signature
	 * @param signedString
	 * @return True or false
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	public Boolean isValidSignature(String signature, String signedString) 
			throws InvalidKeyException, SignatureException, NoSuchAlgorithmException,
			UnsupportedEncodingException {
		
		return RSA.verify(otherPartyPublicKey, signedString, signature);
	}
}
