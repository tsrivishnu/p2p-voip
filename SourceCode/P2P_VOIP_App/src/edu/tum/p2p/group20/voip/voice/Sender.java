package edu.tum.p2p.group20.voip.voice;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
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
import edu.tum.p2p.group20.voip.com.ModuleValidator;
import edu.tum.p2p.group20.voip.crypto.RSA;
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
        	
        	// get hostkey
        	KeyPair hostKeyPair = RSA.getKeyPairFromFile("/Users/Munna/Desktop/sender_private.pem");        	
        	PublicKey otherPartyPublicKey = RSA.getPublicKey("/Users/Munna/Desktop/receiver.pub");
        	String hostPseudoIdentity = "dc429ac06ffec501db88cbed0c5c685d82542c927f0fb3e28b4845be16156dea";
        	String otherPartyPseudoIdentity = "9caf4058012a33048ca50550e8e32285c86c8f3013091ff7ae8c5ea2519c860c";
        	
        	MessageCrypto messageCrypto = new MessageCrypto(hostKeyPair, otherPartyPublicKey, hostPseudoIdentity, otherPartyPseudoIdentity);
        	
        	java.util.Date lastTimestamp = new java.util.Date();
        	
        	// Send initial ping to check module.
        	ModuleValidator moduleValidator = new ModuleValidator();
        	Message initialModuleCheck = new Message(messageCrypto);
        	initialModuleCheck.put("type", "PING");
        	initialModuleCheck.put("verificationHash", moduleValidator.digest);
        	initialModuleCheck.put("verificationTimestamp", moduleValidator.timestampString);
        	// Do not sign this message, cause the receiver won't have your public key yet.
        	out.println(initialModuleCheck.asJSONStringForExchange(true, false));
        	
        	//Receive ping reply
        	inputLine = in.readLine();            	
        	Message pingReplyMessage = new Message(inputLine, false, messageCrypto);
        	if (!pingReplyMessage.isValid(lastTimestamp)) {
        		throw new Exception("Message validation failed");
        	}
        	lastTimestamp = pingReplyMessage.timestamp();
        	System.out.println(pingReplyMessage.get("type"));
        	
        	// Send DH with sender receiver.
        	
        	
        	
        	// Generate and initiate DH
        	SessionKeyManager receiverKeyManager = SessionKeyManager.makeInitiator();
        	        	
        	Message dhInitMessage = new Message(messageCrypto);
        	dhInitMessage.put("type", "DH_INIT");
        	dhInitMessage.put("DHPublicKey", receiverKeyManager.base64PublicDHKeyString());
        	out.println(dhInitMessage.asJSONStringForExchange());
        	
        	// Receive other parties DHpublickey 
        	inputLine = in.readLine();        	
        	Message receivedDhMessage = new Message(inputLine, false, messageCrypto);
        	if (!receivedDhMessage.isValid(lastTimestamp)) {
        		throw new Exception("Message validation failed");
        	}        	
        	String dhPublicKeyString = (String) receivedDhMessage.get("DHPublicKey");
        	lastTimestamp = receivedDhMessage.timestamp();
        	System.out.println(receivedDhMessage.get("type"));

        	byte[] sessionKey = receiverKeyManager.makeSessionKey(dhPublicKeyString);
        	messageCrypto.setSessionKey(sessionKey);
        	System.out.println("SessionKey: "+Base64.encodeBase64String(sessionKey));        	
        	
        	// Send CALL_INIT.
        	Message callInitMessage = new Message(messageCrypto);
        	callInitMessage.put("type", "CALL_INIT");
        	callInitMessage.encrypt();
        	out.println(callInitMessage.asJSONStringForExchange());
        	
        	// Read CALL_INIT_ACK
        	inputLine = in.readLine();
        	Message receivedMessage = new Message(inputLine, true, messageCrypto);
        	if (!receivedMessage.isValid(lastTimestamp)) {
        		throw new Exception("Message validation failed");
        	}
        	receivedMessage.decrypt();
        	System.out.println(receivedMessage.get("type"));
        	
        	// Show waiting to the user here!
        	System.out.println("Waiting for receiver to accept the call...");
        	
        	// Read CALL_ACCEPT/ CALL_DECLINE
        	inputLine = in.readLine();            	
        	Message callAcceptMessage = new Message(inputLine, true, messageCrypto);
        	if (!callAcceptMessage.isValid(lastTimestamp)) {
        		throw new Exception("Message validation failed");
        	}
        	callAcceptMessage.decrypt();
        	lastTimestamp = callAcceptMessage.timestamp();
        	System.out.println(callAcceptMessage.get("type"));
        	

        } catch (IOException e) {
            System.out.println("Exception caught when trying to connect on port " + portNumber);
            System.out.println(e.getMessage());
        }
    }
}