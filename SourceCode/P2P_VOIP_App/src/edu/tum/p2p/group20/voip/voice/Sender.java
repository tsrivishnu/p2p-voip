package edu.tum.p2p.group20.voip.voice;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.Scanner;

import org.apache.commons.codec.binary.Base64;

import edu.tum.p2p.group20.voip.com.Message;
import edu.tum.p2p.group20.voip.com.MessageCrypto;
import edu.tum.p2p.group20.voip.com.ModuleValidator;
import edu.tum.p2p.group20.voip.config.ConfigParser;
import edu.tum.p2p.group20.voip.crypto.RSA;
import edu.tum.p2p.group20.voip.crypto.SHA2;
import edu.tum.p2p.group20.voip.dh.SessionKeyManager;

// Sender is basically a client in the TCP Client-Server paradigm.
// It knows that a receiver is listening for new connections and it can send
//	and receive messages.
// To be more precise, it is the caller, who is to call a receiver.

public class Sender {

	private Socket socket;

	private CallInitiatorListener callInitiatorListener;

	private ConfigParser configParser;

	private PrintWriter out;

	private KeyPair hostKeyPair;

	private String hostPublicKeyEncoded;

	private RSAPublicKey otherPartyPublicKey;

	private String hostPseudoIdentity;

	private String otherPartyPseudoIdentity;

	private MessageCrypto messageCrypto;

	private Date lastTimestamp;
    
    public void initiateCall(String otherPartyPseudoIdentity,RSAPublicKey otherPartyPublicKey, String destinationIP,ConfigParser parser) throws IllegalStateException, Exception {
        configParser = parser;
        try {
        	//TODO: check which IP is to be used here TUN IP or Destination IP from result of OUTGOING_TUNNEL_READY
            socket = new Socket(InetAddress.getByName(destinationIP), 
            		configParser.getVoipPort(),
            		InetAddress.getByName(configParser.getTunIP()),0);
        	
            out = new PrintWriter(socket.getOutputStream(), true);                   
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        	
        	String inputLine;
        	
        	// get hostkey
        	//will come from cmd line or settings
        	hostKeyPair = RSA.getKeyPairFromFile(configParser.getHostKey());
        	hostPublicKeyEncoded = Base64.encodeBase64String(hostKeyPair.getPublic().getEncoded());
        	//TODO: check what comes from UI the public key or the pseudoID
        	this.otherPartyPublicKey = otherPartyPublicKey;
        	SHA2 sha2 = new SHA2();
        	hostPseudoIdentity = sha2.makeSHA2Hash(hostPublicKeyEncoded);
        	//get this from UI
        	this.otherPartyPseudoIdentity = otherPartyPseudoIdentity;
        	
        	messageCrypto = new MessageCrypto(hostKeyPair, otherPartyPublicKey, hostPseudoIdentity, otherPartyPseudoIdentity);
        	
        	lastTimestamp = new Date();
        	
        	// Send initial ping to check module.
        	ModuleValidator moduleValidator = new ModuleValidator();
        	Message initialModuleCheck = new Message(messageCrypto);
        	initialModuleCheck.put("type", "PING");
        	initialModuleCheck.put("verificationHash", moduleValidator.digest);
        	initialModuleCheck.put("verificationTimestamp", moduleValidator.timestampString);
        	initialModuleCheck.put("senderPublicKey", hostPublicKeyEncoded);
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
        	if("PING_BUSY".equals(pingReplyMessage.get("type"))){
        		//the remote peer is busy
        		//show this info to user and stop the call
        		//TODO: make new method to show remote party busy
        		callInitiatorListener.onCallDisconnected("The remote user is busy");
        		return;
        		
        	}
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
        	lastTimestamp = callAcceptMessage.timestamp();
        	if (!callAcceptMessage.isValid(lastTimestamp)) {
        		//Invalid message
        		
        		throw new Exception("Message validation failed");
        	}
        	callAcceptMessage.decrypt();
        	System.out.println(callAcceptMessage.get("type"));
        	if("CALL_ACCEPT".equals(callAcceptMessage.get("type"))){
        		//call was accepted by remote party
            	callInitiatorListener.onCallAccepted(otherPartyPseudoIdentity);
        	} else if("CALL_DECLINE".equals(callAcceptMessage.get("type"))){
        		//call was accepted by remote party
            	callInitiatorListener.onCallDeclined(otherPartyPseudoIdentity);
        	} else{
        		
        	}
        	//TODO: create a loop in new thread for continuously receiving other control messages
        	//TODO: create other methods to send messages 
        	
        	

        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

	public void setCallInitiatorListener(
			CallInitiatorListener callInitiatorListener) {
		// TODO Auto-generated method stub
		this.callInitiatorListener=callInitiatorListener;
		
	}
	
	public void disconnectCall(){
		
		try {
			// Send CALL_DISCONNECT.
        	Message disconnectMsg = new Message(messageCrypto);
        	disconnectMsg.put("type", "CALL_DISCONNECT");
        	disconnectMsg.encrypt();
        	out.println(disconnectMsg.asJSONStringForExchange());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			if(socket!=null){
				try {
					socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				socket=null;
			}
		}
	}
}