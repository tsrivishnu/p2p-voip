package edu.tum.p2p.group20.voip.voice;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.spec.InvalidParameterSpecException;
import java.util.*;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.apache.commons.codec.binary.Base64;

import edu.tum.p2p.group20.voip.dh.SessionKeyManager;

// Sender is basically a client in the TCP Client-Server paradigm.
// It knows that a receiver is listening for new connections and it can send
//	and receive messages.
// To be more precise, it is the caller, who is to call a receiver.

public class Sender {
    public static void main(String[] args)
    		throws IllegalStateException, Exception {
        
        if (args.length != 1) {
            System.err.println("Usage: java Sender <port number>");
            System.exit(1);
        }
         
        int portNumber = Integer.parseInt(args[0]);
         
        try (
            Socket socket = new Socket("127.0.0.1", Integer.parseInt(args[0]));
        	
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);                   
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        	Scanner userIn = new Scanner(System.in);
        ) {
        	String inputLine;
        	String inputFromUser;
        	
        	
        	// Generate and initiate DH
        	SessionKeyManager receiverKeyManager = SessionKeyManager.makeInitiator();
        	
        	JSONObject message = new JSONObject();
        	JSONObject dhJSON = new JSONObject();        	
        	dhJSON.put("DHPublicKey", receiverKeyManager.base64PublicDHKeyString());
        	message.put("message", dhJSON);
        	
        	out.println(message.toJSONString());
        	
        	// Receive other parties DHpublickey 
        	inputLine = in.readLine();
        	
        	JSONParser parser = new JSONParser();        	
        	JSONObject message2 = (JSONObject) parser.parse(inputLine);
        	message2 = (JSONObject) message2.get("message");
        	String publicKeyString = (String) message2.get("DHPublicKey");

        	byte[] sessionKey = receiverKeyManager.makeSessionKey(publicKeyString);        
        	System.out.println(Base64.encodeBase64String(sessionKey));
        	
        	
        	
        	
        	// RSA encrypt a string and send it.
        	// SHA-256 hash of sessionKey
        	MessageDigest md = MessageDigest.getInstance("SHA-256");
        	md.update(sessionKey);
        	sessionKey = md.digest();
        	
        	Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            
        	SecretKeySpec key = new SecretKeySpec(sessionKey, "AES");

            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
            
            String toEncrypt = "this is encrypted";
            byte[] toEncryptBytes = toEncrypt.getBytes();
            System.out.println(sessionKey.length);
            
            // encryption pass
            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] cipherText = new byte[cipher.getOutputSize(toEncryptBytes.length)];
            int ctLength = cipher.update(toEncryptBytes, 0, toEncryptBytes.length, cipherText, 0);
            ctLength += cipher.doFinal(cipherText, ctLength);
            System.out.println(new String(cipherText));
            out.println(Base64.encodeBase64String(cipherText));
            System.out.println(ctLength);
            System.out.println(cipherText.length);

        } catch (IOException e) {
            System.out.println("Exception caught when trying to connect on port " + portNumber);
            System.out.println(e.getMessage());
        }
    }
}