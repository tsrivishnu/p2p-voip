package edu.tum.p2p.group20.voip.crypto;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.SignatureException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMException;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

public class RsaCheck {

	public static void main(String[] args) throws IOException, GeneralSecurityException {
		
		KeyPair kp = RSA.getKeyPairFromFile("/Users/Munna/Desktop/sender_private.pem");

		System.out.println(kp.getPrivate().getClass().getName());
		System.out.println(kp.getPublic().getClass().getName());
		
		String signature = RSA.sign(kp.getPrivate(), "this is me");
		
		System.out.println(RSA.verify(kp.getPublic(), "this is me!", signature));
		
		//Receiver
		KeyPair kp2 = RSA.getKeyPairFromFile("/Users/Munna/Desktop/receiver_private.pem");
		
		String cipher = RSA.encrypt("This si raw test", kp2.getPublic());
		System.out.println(RSA.decrypt(cipher, kp2.getPrivate()));
	}

}
