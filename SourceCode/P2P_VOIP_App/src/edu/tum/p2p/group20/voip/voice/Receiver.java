package edu.tum.p2p.group20.voip.voice;
import java.net.*;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.ShortBufferException;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.parser.ParseException;

import edu.tum.p2p.group20.voip.com.Message;
import edu.tum.p2p.group20.voip.com.MessageCrypto;
import edu.tum.p2p.group20.voip.com.ModuleValidator;
import edu.tum.p2p.group20.voip.config.ConfigParser;
import edu.tum.p2p.group20.voip.crypto.RSA;
import edu.tum.p2p.group20.voip.crypto.SHA2;
import edu.tum.p2p.group20.voip.dh.SessionKeyManager;

// Receiver is basically a Server in the TCP Client-Server paradigm.
// It listens to receive messages on a socket.
// To be more precise, it is the callee, who is ready to receive calls.

public class Receiver extends Thread {
	
	//Listener for call notifications
	private CallReceiverListener callReceiverListener;
	private Socket clientSocket;//socket if some-remote party tries to make a connection to TUN device
	private KeyPair hostKeyPair;//Localhost Public-Private key pair
	private int portNumber;
	private InetAddress bindAddress;
	private boolean stop;//flag to quit the thread loop
	private static int status=1;//status of the receiver
	private final static int IDLE=1;//no incoming call
	private final static int BUSY=2;//establishing incoming call
	private final static int WAIT=3;//incoming call is already existing
	protected static final long HEARTBEAT_TIMEOUT = 15000;
	
	private ConfigParser configParser;
	private Date lastHeartBeat;
	private String otherPartyPseudoIdentity;
	
	/**
	 * @param clientSocket2
	 */
	public Receiver(Socket socket,ConfigParser parser, CallReceiverListener listener) {
		
		clientSocket = socket;
		configParser = parser;
		callReceiverListener = listener;
	}

	
	@Override
    public void run() {
         
         
        try {
  
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);                   
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
         	
            String inputLine;
            
            KeyPair hostKeyPair = RSA.getKeyPairFromFile(configParser.getHostKey());
            PublicKey otherPartyPublicKey = null; // We get to know this from PING message.
        	otherPartyPseudoIdentity = null; // We get to know this from PING message.
        	//TODO: hash the public key of hostKeyPair to get hostPseudoIdentity
        	PublicKey hostPublicKey = hostKeyPair.getPublic();
        	
        	
        	SHA2 sha2 = new SHA2();
        	//TODO: check how to get this host public key as a string
        	String hostPseudoIdentity = Base64.encodeBase64String(sha2.makeSHA2Hash(hostPublicKey.getEncoded())); 
        	
        	MessageCrypto messageCrypto = new MessageCrypto(hostKeyPair, otherPartyPublicKey, hostPseudoIdentity, otherPartyPseudoIdentity);
        	
        	Date lastTimestamp;
        	
            while ((inputLine = in.readLine()) != null) {

            	// Receive a module verification PING
            	Message receivedPingMessage = new Message(inputLine, false, messageCrypto);            	
            	ModuleValidator moduleValidator =  new ModuleValidator(
					(String) receivedPingMessage.get("verificationTimestamp"),
					(String) receivedPingMessage.get("verificationHash")
				);
            	if (!moduleValidator.isValid()) {
            		//TODO: add log4j here
            		//quiting this thread
            		clientSocket.close();
            		return;
            		//throw new Exception("Voip Module Verification Fail");
            	}
            	System.out.println(receivedPingMessage.get("type"));
            	
            	// Learn caller's public key and pseudoIdentity
            	// TODO These details need to be verified somehow! We now don't know if the
            	// 		caller is who he says he is. Probably, make pseudo-identity some 
            	//      kind of hash of public key? 
            	otherPartyPseudoIdentity = (String) receivedPingMessage.get("sender");
            	System.out.println(otherPartyPseudoIdentity);
            	
            	
            	messageCrypto.otherPartyPseudoIdentity = otherPartyPseudoIdentity; 
            	messageCrypto.otherPartyPublicKey = RSA.getPublicKeyFromString((String) receivedPingMessage.get("senderPublicKey"));
            	lastTimestamp = receivedPingMessage.timestamp();
            	
            	
            	if(status==IDLE){
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
	            		//TODO: add log4j here
	            		//quiting this thread
	            		clientSocket.close();
	            		return;
	            		//throw new Exception("Message validation failed");
	            	}
	            	System.out.println(receivedDhMessage.get("type"));
	            	String senderPublicKeyString = (String) receivedDhMessage.get("DHPublicKey");

	            	SessionKeyManager receiverKeyManager = SessionKeyManager.makeSecondParty(senderPublicKeyString);
	            	byte [] sessionKey = receiverKeyManager.makeSessionKey(senderPublicKeyString);
	            	
	            	System.out.println("SessionKey: "+Base64.encodeBase64String(sessionKey));
	            	            	
	            	messageCrypto.setSessionKey(sessionKey,false);
	            	
	            	// Send your dh params to the other party.
	            	Message dhPublicMessage = new Message(messageCrypto);
	            	dhPublicMessage.put("type", "DH_REPLY");
	            	dhPublicMessage.put("DHPublicKey", receiverKeyManager.base64PublicDHKeyString());        	
	            	out.println(dhPublicMessage.asJSONStringForExchange());
	               	
	            	// Read CALL_INIT
	            	inputLine = in.readLine();            	
	            	Message receivedMessage = new Message(inputLine, true, messageCrypto);
	            	if (!receivedMessage.isValid(lastTimestamp)) {
	            		//TODO: add log4j here
	            		//quiting this thread
	            		clientSocket.close();
	            		return;
	            		//throw new Exception("Message validation failed");
	            	}
	            	receivedMessage.decrypt();
	            	lastTimestamp = receivedMessage.timestamp();
	            	System.out.println(receivedMessage.get("type"));
	            	
	            	 // Send CALL_INIT_ACK.
	            	Message callInitAckMessage = new Message(messageCrypto);
	            	callInitAckMessage.put("type", "CALL_INIT_ACK");
	            	callInitAckMessage.encrypt();
	            	out.println(callInitAckMessage.asJSONStringForExchange());            
	            	
	            	
	            	
	            	// Show incoming call to the user and ask him to accept the call!
	            
	            	boolean accept = callReceiverListener.onIncomingCall(otherPartyPseudoIdentity,sessionKey);

	            	if (accept) {
	            		// Send CALL_ACCEPT
	                	Message callAcceptMessage = new Message(messageCrypto);
	                	callAcceptMessage.put("type", "CALL_ACCEPT");
	                	callAcceptMessage.encrypt();
	                	out.println(callAcceptMessage.asJSONStringForExchange());
	                	//send call connected callback
	                	
	                	callReceiverListener.onCallConnected(otherPartyPseudoIdentity,sessionKey);
	                	
	                	// create a timertask to check heartbeat timestamps
	                	lastHeartBeat =  new Date();
	                	Timer heartBeatTimer = new Timer();
	                	TimerTask heartBeatSender = new TimerTask() {
	    					
	    					@Override
	    					public void run() {
	    						
	    						//check heartbeat timestamp
	    						if((new Date().getTime()-lastHeartBeat.getTime())>HEARTBEAT_TIMEOUT){
	    							//last heart beat too old
	    							this.cancel();//stop heartbeat timer task
	    							System.out.println("HEARTBEAT TIMEOUT");
	    							stop=true;
	    	                		callReceiverListener.onCallDisconnected(otherPartyPseudoIdentity);
	    							return;
	    						}
	    						
	    					}
	    				};
	    				//schedule heartBeat checking timer after 4 sec for every 10 sec
	                	heartBeatTimer.schedule(heartBeatSender, 4000, 10000);
	                	while(!stop){
	                		// Read disconnect or Heartbeat message
	    	            	inputLine = in.readLine();
	    	            	//connection is broken
	    	            	if(inputLine==null){
	    	            		stop=true;
	    	            		callReceiverListener.onCallDisconnected("Invalid message received from client!");
	    	            		return;
	    	            	}
	    	            	Message newMessage = new Message(inputLine, true, messageCrypto);
	    	            	if (!newMessage.isValid(lastTimestamp)) {
	    	            		//TODO: Disconnect call as invalid message arrived
	    	            		stop=true;
	    	            		callReceiverListener.onCallDisconnected("Invalid message received from client!");
	    	            		return;
	    	            	} else {
	    	            		newMessage.decrypt();
	    	            		lastTimestamp = newMessage.timestamp();
	    		            	System.out.println(newMessage.get("type"));
	    		            	if("HEARTBEAT".equals(newMessage.get("type"))){
	    		            		//update last sheartbeat timestamp
	    		            		lastHeartBeat= lastTimestamp;
	    		            		//send heartbeat acknowledgement
	    		            		Message heartbeatAck = new Message(messageCrypto);
	    		            		heartbeatAck.put("type", "HEARTBEAT_ACK");
	    		            		heartbeatAck.encrypt();
	    		                	out.println(heartbeatAck.asJSONStringForExchange());
	    		            	} else if ("CALL_DISCONNECT".equals(newMessage.get("type"))){
	    		            		stop=true;
	    		            		callReceiverListener.onCallDisconnected("Remote client disconnected call!");
	    		            		return;
	    		            	}
	    	            	}
	                	}
	            	} else {
	            		// Send CALL_DECLINE
	                	Message callAcceptMessage = new Message(messageCrypto);
	                	callAcceptMessage.put("type", "CALL_DECLINE");
	                	callAcceptMessage.encrypt();
	                	out.println(callAcceptMessage.asJSONStringForExchange());
	                	stop=true;
	                	return;
	            	}
            	}else if(status==BUSY){
            		//Send busy message and let remote user close the socket           	
	            	moduleValidator = new ModuleValidator();
	            	Message pingReply = new Message(messageCrypto);
	            	pingReply.put("type", "PING_REPLY_BUSY");
	            	pingReply.put("verificationHash", moduleValidator.digest);
	            	pingReply.put("verificationTimestamp", moduleValidator.timestampString);
	            	out.println(pingReply.asJSONStringForExchange());
	            	stop=true;
	            	//close connection
	            	if(clientSocket!=null){
						clientSocket.close();
					}
	            	
	            	return;
            	} else {
            		//TODO: Implement waiting status and further flow
            		//status==WAIT
            		//Send WAIT message and let user keep waiting or cut the call
            		//next message also should be sent by us
            	}
            	
            }
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        } catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ShortBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void disconnectCall(){
    	//TODO: Send disconnect message to clientSocket
    	if(clientSocket!=null){
    		try {
				clientSocket.close();
				clientSocket=null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
    }
    
  
    
    

}