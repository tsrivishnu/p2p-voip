/**
 * 
 */
package edu.tum.p2p.group20.voip.testing;

import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.interfaces.RSAPublicKey;

import edu.tum.p2p.group20.voip.config.ConfigParser;
import edu.tum.p2p.group20.voip.crypto.RSA;
import edu.tum.p2p.group20.voip.voice.CallInitiatorListener;
import edu.tum.p2p.group20.voip.voice.Sender;

import org.apache.commons.codec.binary.Base64;


/**
 * @author anshulvij
 *
 */
public class TestingSender {

	private static Sender sender;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		sender = new Sender();
		sender.setCallInitiatorListener(new CallInitiatorListener() {
			
			@Override
			public void onCallInitiated(String pseudoId) {

				System.out.println("CallInitiated");
			}
			
			@Override
			public void onCallDisconnected(String pseudoId) {
				System.out.println("onCallDisconnected");

			}
			
			@Override
			public void onCallDeclined(String otherPartyPseudoIdentity) {
				System.out.println("onCallDeclined");
			}
			
			
			@Override
			public void onCallAccepted(String pseudoId,byte[] sessionKey) {
				// TODO Auto-generated method stub
				System.out.println("onCallAccepted");
				System.out.println("sessionKey="+Base64.encodeBase64String(sessionKey));
			}
		});
		
		ConfigParser parser;
		try {
			parser = ConfigParser.getInstance("lib/test_sample_app_config2.ini");
			KeyPair hostKeyPair = RSA.getKeyPairFromFile("lib/receiver_private.pem");
	    	RSAPublicKey remotePublicKey = (RSAPublicKey) hostKeyPair.getPublic();
	    	MessageDigest md = MessageDigest.getInstance("SHA-256");
	    	String receiverPseudoId = Base64.encodeBase64String(md.digest(remotePublicKey.getEncoded()));
	    	//TOOD: get this IP from TUN_READY destination IP
			sender.initiateCall(receiverPseudoId, remotePublicKey,"192.168.1.4", parser);
		} catch ( Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}



}
