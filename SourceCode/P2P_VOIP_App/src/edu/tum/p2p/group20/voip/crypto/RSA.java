// Class references from: http://stackoverflow.com/a/27621696/976880
package edu.tum.p2p.group20.voip.crypto;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

public class RSA {
	
	private static PEMParser pp;

	public static KeyPair getKeyPairFromFile(String keyPath) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(keyPath));
		Security.addProvider(new BouncyCastleProvider());
		pp = new PEMParser(br);
		PEMKeyPair pemKeyPair = (PEMKeyPair) pp.readObject();
		KeyPair kp = new JcaPEMKeyConverter().getKeyPair(pemKeyPair);
		pp.close();
		return kp;
	}
	
	private static String getKey(String filename) throws IOException {
	    // Read key from file
	    String strKeyPEM = "";
	    BufferedReader br = new BufferedReader(new FileReader(filename));
	    String line;
	    while ((line = br.readLine()) != null) {
	        strKeyPEM += line + "\n";
	    }
	    br.close();
	    return strKeyPEM;
	}
	
	public static RSAPublicKey getPublicKey(String filename) throws IOException, GeneralSecurityException {
	    String publicKeyPEM = getKey(filename);
	    
	    return getPublicKeyFromString(publicKeyPEM);		
	}
	
	public static RSAPublicKey getPublicKeyFromString(String key) throws IOException, GeneralSecurityException {
	    String publicKeyPEM = key;
	    publicKeyPEM = publicKeyPEM.replace("-----BEGIN PUBLIC KEY-----\n", "");
	    publicKeyPEM = publicKeyPEM.replace("-----END PUBLIC KEY-----", "");
	    byte[] encoded = Base64.decodeBase64(publicKeyPEM);
	    KeyFactory kf = KeyFactory.getInstance("RSA");
	    RSAPublicKey pubKey = (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(encoded));
	    return pubKey;
	}
	
	public static RSAPublicKey getPublicKeyFromBytes(byte[] publickKeyBytes) throws InvalidKeySpecException, NoSuchAlgorithmException {
		return (RSAPublicKey) KeyFactory.getInstance("RSA")
				.generatePublic(new X509EncodedKeySpec(publickKeyBytes));
	}
	
	public static String sign(PrivateKey privateKey, String message) throws InvalidKeyException, SignatureException {
	    
	    
	    try {
	    	Signature sign = Signature.getInstance("SHA1withRSA");
		    sign.initSign(privateKey);
			sign.update(message.getBytes("UTF-8"));
			
			return new String(Base64.encodeBase64(sign.sign()), "UTF-8");			
		} catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
			
			e.printStackTrace();
		}
	    
	    return null;
	}
	
	public static byte[] sign(PrivateKey privateKey, byte[] messageBytes) throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
		Signature sign = Signature.getInstance("SHA1withRSA");
		sign.initSign(privateKey);
		sign.update(messageBytes);
		return sign.sign();
	}
	
	
	public static boolean verify(PublicKey publicKey, String message, String signature) throws SignatureException, NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {
	    Signature sign = Signature.getInstance("SHA1withRSA");
	    sign.initVerify(publicKey);
	    sign.update(message.getBytes("UTF-8"));
	    return sign.verify(Base64.decodeBase64(signature.getBytes("UTF-8")));
	}
	
	public static boolean verify(PublicKey publicKey, byte[] messageBytes, byte[] signatureBytes) throws SignatureException, NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {
	    Signature sign = Signature.getInstance("SHA1withRSA");
	    sign.initVerify(publicKey);
	    sign.update(messageBytes);
	    return sign.verify(signatureBytes);
	}
	
	public static String encrypt(String rawText, PublicKey publicKey) throws IOException, GeneralSecurityException {
	    Cipher cipher = Cipher.getInstance("RSA");
	    cipher.init(Cipher.ENCRYPT_MODE, publicKey);
	    return Base64.encodeBase64String(cipher.doFinal(rawText.getBytes("UTF-8")));
	}
	
	public static String decrypt(String cipherText, PrivateKey privateKey) throws IOException, GeneralSecurityException {
	    Cipher cipher = Cipher.getInstance("RSA");
	    cipher.init(Cipher.DECRYPT_MODE, privateKey);
	    return new String(cipher.doFinal(Base64.decodeBase64(cipherText)), "UTF-8");
	}
}