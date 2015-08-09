package edu.tum.p2p.group20.voip.voice;
import java.net.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.util.Scanner;
import java.io.*;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import edu.tum.p2p.group20.voip.com.Message;
import edu.tum.p2p.group20.voip.com.MessageCrypto;
import edu.tum.p2p.group20.voip.com.ModuleValidator;
import edu.tum.p2p.group20.voip.crypto.RSA;
import edu.tum.p2p.group20.voip.dh.SessionKeyManager;

// Receiver is basically a Server in the TCP Client-Server paradigm.
// It listens to receive messages on a socket.
// To be more precise, it is the callee, who is ready to receive calls.

public class Receiver {
    public static void main(String[] args) throws IllegalStateException, Exception {
         
        if (args.length != 1) {
            System.err.println("Usage: java Receiver <port number>");
            System.exit(1);
        }
         
        int portNumber = Integer.parseInt(args[0]);
         
        try (
            ServerSocket serverSocket = new ServerSocket(Integer.parseInt(args[0]));
            Socket clientSocket = serverSocket.accept();        	
        	
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);                   
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        	Scanner userIn = new Scanner(System.in);
        ) {
            String inputLine;
            String inputFromUser;
            
            KeyPair hostKeyPair = RSA.getKeyPairFromFile("lib/receiver_private.pem");
            PublicKey otherPartyPublicKey = null; // We get to know this from PING message.
        	String otherPartyPseudoIdentity = null; // We get to know this from PING message.
        	String hostPseudoIdentity = "9caf4058012a33048ca50550e8e32285c86c8f3013091ff7ae8c5ea2519c860c";
        	
        	MessageCrypto messageCrypto = new MessageCrypto(hostKeyPair, otherPartyPublicKey, hostPseudoIdentity, otherPartyPseudoIdentity);
        	
        	java.util.Date lastTimestamp;
        	
            while ((inputLine = in.readLine()) != null) {

            	// Receive a module verification PING
            	Message receivedPingMessage = new Message(inputLine, false, messageCrypto);            	
            	ModuleValidator moduleValidator =  new ModuleValidator(
					(String) receivedPingMessage.get("verificationTimestamp"),
					(String) receivedPingMessage.get("verificationHash")
				);
            	if (!moduleValidator.isValid()) {
            		throw new Exception("Voip Module Verification Fail");
            	}
            	System.out.println(receivedPingMessage.get("type"));
            	
            	// Learn caller's public key and pseudoIdentity
            	// TODO These details need to be verified somehow! We now don't know if the
            	// 		caller is who he says he is. Probably, make pseudo-identity some 
            	//      kind of hash of public key? 
            	otherPartyPseudoIdentity = (String) receivedPingMessage.get("sender");
            	messageCrypto.otherPartyPseudoIdentity = otherPartyPseudoIdentity; 
            	messageCrypto.otherPartyPublicKey = RSA.getPublicKeyFromString((String) receivedPingMessage.get("senderPublicKey"));
            	lastTimestamp = receivedPingMessage.timestamp();
            	
            	// Send PING_REPLY with module verification            	
            	moduleValidator = new ModuleValidator();
            	Message pingReply = new Message(messageCrypto);
            	pingReply.put("type", "PING_REPLY");
            	pingReply.put("verificationHash", moduleValidator.digest);
            	pingReply.put("verificationTimestamp", moduleValidator.timestampString);
            	out.println(pingReply.asJSONStringForExchange());
            	
            	// Receive other parties DHPublicKey data
            	inputLine = in.readLine();
            	Message receivedDhMessage = new Message(inputLine, false, messageCrypto);
            	if (!receivedDhMessage.isValid(lastTimestamp)) {
            		throw new Exception("Message validation failed");
            	}
            	System.out.println(receivedDhMessage.get("type"));
            	String senderPublicKeyString = (String) receivedDhMessage.get("DHPublicKey");

            	SessionKeyManager receiverKeyManager = SessionKeyManager.makeSecondParty(senderPublicKeyString);
            	byte [] sessionKey = receiverKeyManager.makeSessionKey(senderPublicKeyString);
            	
            	System.out.println("SessionKey: "+Base64.encodeBase64String(sessionKey));
            	            	
            	messageCrypto.setSessionKey(sessionKey);
            	
            	// Send your dh params to the other party.
            	Message dhPublicMessage = new Message(messageCrypto);
            	dhPublicMessage.put("type", "DH_REPLY");
            	dhPublicMessage.put("DHPublicKey", receiverKeyManager.base64PublicDHKeyString());        	
            	out.println(dhPublicMessage.asJSONStringForExchange());
               	
            	// Read CALL_INIT
            	inputLine = in.readLine();            	
            	Message receivedMessage = new Message(inputLine, true, messageCrypto);
            	if (!receivedMessage.isValid(lastTimestamp)) {
            		throw new Exception("Message validation failed");
            	}
            	receivedMessage.decrypt();
            	lastTimestamp = receivedMessage.timestamp();
            	System.out.println(receivedMessage.get("type"));
            	
            	 // Send CALL_INIT_ACK.
            	Message callInitAckMessage = new Message(messageCrypto);
            	callInitAckMessage.put("type", "CALL_INIT_ACK");
            	callInitAckMessage.encrypt();
            	out.println(callInitAckMessage.asJSONStringForExchange());            
            	
            	// Show ringing to the user and ask him to accep the call!
            	System.out.println("Incoming call: Accept? (y/n): ");

        		inputFromUser = userIn.nextLine();
        		inputFromUser.replaceAll("(\\r|\\n)", "");
            	if (inputFromUser.equals("y")) {

            		// Send CALL_ACCEPT
                	Message callAcceptMessage = new Message(messageCrypto);
                	callAcceptMessage.put("type", "CALL_ACCEPT");
                	callAcceptMessage.encrypt();
                	out.println(callAcceptMessage.asJSONStringForExchange());            		
            	} else if (inputFromUser.equals('n')) {

            		// Send CALL_DECLINE
                	Message callAcceptMessage = new Message(messageCrypto);
                	callAcceptMessage.put("type", "CALL_DECLINE");
                	callAcceptMessage.encrypt();
                	out.println(callAcceptMessage.asJSONStringForExchange());
            	}
            }
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }
}