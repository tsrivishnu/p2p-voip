package edu.tum.p2p.group20.voip.dh;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

/**
 * Class to contain various helpers needed during the Diffie-Hellman key 
 * exchange/agreement.
 * 
 * @author Sri Vishnu Totakura <srivishnu@totakura.in>
 *
 */
public class Helpers {
	private static String algorithm = Config.algorithm;
	
	/**
	 * Static method to return diffie-hellman public key instance from a byte 
	 * encoded public key.
	 *  
	 * @param byteEncodedPublicKey 
	 * @return PublicKey instance which contains all the diffie-hellman public parameters and key.
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public static PublicKey publicKeyFromByteEncoded(byte[] byteEncodedPublicKey)
		  throws NoSuchAlgorithmException, InvalidKeySpecException {
		
		X509EncodedKeySpec ks = new X509EncodedKeySpec(byteEncodedPublicKey);
		KeyFactory kf = KeyFactory.getInstance(algorithm);
		return kf.generatePublic(ks);
	}
}