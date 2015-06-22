package edu.tum.p2p.group20.voip.voice;
import java.net.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.io.*;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
            while ((inputLine = in.readLine()) != null) {

            	// Receive other parties DHPublicKey data
            	JSONParser parser = new JSONParser();            	
            	JSONObject message = (JSONObject) parser.parse(inputLine);
            	message = (JSONObject) message.get("message");
            	String publicKeyString = (String) message.get("DHPublicKey");

            	SessionKeyManager receiverKeyManager = SessionKeyManager.makeSecondParty(publicKeyString);

            	System.out.println(Base64.encodeBase64String(receiverKeyManager.makeSessionKey(publicKeyString)));            	

            	// send public key to other party
            	JSONObject message2 = new JSONObject();
            	JSONObject dhJSON = new JSONObject();
            	
            	dhJSON.put("DHPublicKey", receiverKeyManager.base64PublicDHKeyString());
            	message.put("message", dhJSON);
            	
            	out.println(message.toJSONString());

            }
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }
}