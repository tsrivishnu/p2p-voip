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
        ) {
            String inputLine;
            
            KeyPair hostKeyPair = RSA.getKeyPairFromFile("/Users/Munna/Desktop/receiver_private.pem");        	
        	PublicKey otherPartyPublicKey = RSA.getPublicKey("/Users/Munna/Desktop/sender.pub");
        	String otherPartyPseudoIdentity = "dc429ac06ffec501db88cbed0c5c685d82542c927f0fb3e28b4845be16156dea";
        	String hostPseudoIdentity = "9caf4058012a33048ca50550e8e32285c86c8f3013091ff7ae8c5ea2519c860c";
        	
        	MessageCrypto messageCrypto = new MessageCrypto(hostKeyPair, otherPartyPublicKey, hostPseudoIdentity, otherPartyPseudoIdentity);
        	
        	java.util.Date lastTimestamp = new java.util.Date();
        	
            while ((inputLine = in.readLine()) != null) {

            	// Receive other parties DHPublicKey data
            	Message receivedDhMessage = new Message(inputLine, false, messageCrypto);
            	String publicKeyString = (String) receivedDhMessage.get("DHPublicKey");

            	SessionKeyManager receiverKeyManager = SessionKeyManager.makeSecondParty(publicKeyString);
            	byte [] sessionKey = receiverKeyManager.makeSessionKey(publicKeyString);
            	
            	System.out.println(Base64.encodeBase64String(sessionKey));
            	            	
            	messageCrypto.setSessionKey(sessionKey);
            	
            	// Send your dh params to the other party.
            	Message dhPublicMessage = new Message(messageCrypto);
            	dhPublicMessage.put("DHPublicKey", receiverKeyManager.base64PublicDHKeyString());        	
            	out.println(dhPublicMessage.asJSONStringForExchange());
               	
            	// Read CALL_INIT
            	inputLine = in.readLine();            	
            	Message receivedMessage = new Message(inputLine, true, messageCrypto);
            	System.out.print(receivedMessage.isValid(receivedMessage.timestamp()));
            	receivedMessage.decrypt();
            	System.out.println(receivedMessage.get("type"));
            	
            	 // Send CALL_INIT_ACK.
            	Message callInitMessage = new Message(messageCrypto);
            	callInitMessage.put("type", "CALL_INIT_ACK");
            	callInitMessage.encrypt();
            	out.println(callInitMessage.asJSONStringForExchange());
            }
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }
}