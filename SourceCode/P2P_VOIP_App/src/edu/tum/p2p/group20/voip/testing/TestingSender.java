/**
 * 
 */
package edu.tum.p2p.group20.voip.testing;

import java.io.IOException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;








import edu.tum.p2p.group20.voip.config.ConfigParser;
import edu.tum.p2p.group20.voip.crypto.RSA;
import edu.tum.p2p.group20.voip.voice.CallInitiatorListener;
import edu.tum.p2p.group20.voip.voice.Sender;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.ConfigurationException;

/**
 * @author anshulvij
 *
 */
public class TestingSender {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Sender sender = new Sender();
		sender.setCallInitiatorListener(new CallInitiatorListener() {
			
			@Override
			public void onCallInitiated(String pseudoId) {
				// TODO Auto-generated method stub
				System.out.println("CallInitiated");
			}
			
			@Override
			public void onCallDisconnected(String pseudoId) {
				// TODO Auto-generated method stub
				System.out.println("onCallDisconnected");
			}
			
			@Override
			public void onCallDeclined(String otherPartyPseudoIdentity) {
				// TODO Auto-generated method stub
				System.out.println("onCallDeclined");
			}
			
			@Override
			public void onCallConnected(String pseudoId) {
				// TODO Auto-generated method stub
				System.out.println("onCallConnected");
			}
			
			@Override
			public void onCallAccepted(String pseudoId) {
				// TODO Auto-generated method stub
				System.out.println("onCallAccepted");
				
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
			sender.initiateCall(receiverPseudoId, remotePublicKey,"192.168.1.5", parser);
		} catch ( Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}



}
