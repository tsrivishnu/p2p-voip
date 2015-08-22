package edu.tum.p2p.group20.voip.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA2 {

	public byte[] makeSHA2Hash(String input)

	{
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-256");
			md.reset();
			byte[] buffer = input.getBytes();
			md.update(buffer);
			byte[] digest = md.digest();

			return digest;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;

	}
	
	public byte[] makeSHA2Hash(byte[] input)

	{
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-256");
			md.reset();
			md.update(input);
			byte[] digest = md.digest();
			return digest;
		
		
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;

	}
}