package edu.tum.p2p.group20.voip.voice;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidParameterSpecException;
import java.util.*;

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

        	System.out.println(Base64.encodeBase64String(receiverKeyManager.makeSessionKey(publicKeyString)));
        	

        } catch (IOException e) {
            System.out.println("Exception caught when trying to connect on port " + portNumber);
            System.out.println(e.getMessage());
        }
    }
}