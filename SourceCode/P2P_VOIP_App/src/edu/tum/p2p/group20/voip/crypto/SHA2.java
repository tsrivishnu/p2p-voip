package edu.tum.p2p.group20.voip.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA2 {

	public String makeSHA2Hash(String input)

	{
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-256");
			md.reset();
			byte[] buffer = input.getBytes();
			md.update(buffer);
			byte[] digest = md.digest();

			String hexStr = "";
			//converting bytes to hexString
			for (int i = 0; i < digest.length; i++) {
				hexStr += Integer.toString((digest[i] & 0xff) + 0x100, 16)
						.substring(1);
			}
			return hexStr;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;

	}
	
	public String makeSHA2Hash(byte[] input)

	{
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-256");
			md.reset();
			md.update(input);
			byte[] digest = md.digest();

			String hexStr = "";
			//converting bytes to hexString
			for (int i = 0; i < digest.length; i++) {
				hexStr += Integer.toString((digest[i] & 0xff) + 0x100, 16)
						.substring(1);
			}
			return hexStr;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;

	}
}