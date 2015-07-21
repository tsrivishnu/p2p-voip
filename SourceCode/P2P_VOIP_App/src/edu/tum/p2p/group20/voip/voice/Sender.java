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

import edu.tum.p2p.group20.voip.com.Message;
import edu.tum.p2p.group20.voip.com.MessageCrypto;
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
        	
        	Message dhInitMessage = new Message();
        	dhInitMessage.put("DHPublicKey", receiverKeyManager.base64PublicDHKeyString());        	
        	out.println(dhInitMessage.asJSON());
        	
        	// Receive other parties DHpublickey 
        	inputLine = in.readLine();
        	
        	Message receivedDhMessage = new Message(inputLine, false);
        	String publicKeyString = (String) receivedDhMessage.get("DHPublicKey");

        	byte[] sessionKey = receiverKeyManager.makeSessionKey(publicKeyString);        
        	System.out.println(Base64.encodeBase64String(sessionKey));
        	
        	// SHA-256 hash of sessionKey
        	MessageDigest md = MessageDigest.getInstance("SHA-256");
        	md.update(sessionKey);        	
        	sessionKey = md.digest();
        	
        	MessageCrypto messageCrypto = new MessageCrypto(sessionKey);
        	
        	// Send CALL_INIT message.
        	Message callInitMessage = new Message();
        	callInitMessage.put("type", "CALL_INIT");
        	callInitMessage.messageCrypto = messageCrypto;
        	callInitMessage.encrypt();
        	callInitMessage.sign();
        	out.println(callInitMessage.asJSON());

        } catch (IOException e) {
            System.out.println("Exception caught when trying to connect on port " + portNumber);
            System.out.println(e.getMessage());
        }
    }
}