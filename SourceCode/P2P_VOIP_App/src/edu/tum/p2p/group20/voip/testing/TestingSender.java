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
			public void onCallAccepted(String pseudoId,byte[] sessionKey,String destinationIP) {
				System.out.println("onCallAccepted");
				System.out.println("sessionKey="+Base64.encodeBase64String(sessionKey));
				System.out.println("destinationIP="+destinationIP);
			}

			@Override
			public void onCallFailed(String calleeId) {
				System.out.println("onCallFailed");
			}
		});
		
		ConfigParser parser;
		try {
			parser = ConfigParser.getInstance("test/test_app_config.ini");
			KeyPair remoteKeyPair = RSA.getKeyPairFromFile(parser.getTestRemoteRsaKeyPair());
	    	RSAPublicKey remotePublicKey = (RSAPublicKey) remoteKeyPair.getPublic();
	    	MessageDigest md = MessageDigest.getInstance("SHA-256");
	    	String receiverPseudoId = Base64.encodeBase64String(md.digest(remotePublicKey.getEncoded()));
	    	//TOOD: get this IP from TUN_READY destination IP
			sender.initiateCall(receiverPseudoId, remotePublicKey,parser.getTestDestinatonIp(), parser);
		} catch ( Exception e) {

			e.printStackTrace();
		}
		
	}



}
