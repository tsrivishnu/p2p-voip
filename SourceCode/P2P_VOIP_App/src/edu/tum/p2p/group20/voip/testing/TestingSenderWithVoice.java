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
import edu.tum.p2p.group20.voip.voice.VoicePlayer;
import edu.tum.p2p.group20.voip.voice.VoiceRecorder;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.ConfigurationException;

/**
 * @author anshulvij
 *
 */
public class TestingSenderWithVoice {

	protected static VoiceRecorder voiceRecorder;
	protected static VoicePlayer voicePlayer;
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
				if(voicePlayer!=null){
					voicePlayer.stopSound();
					voicePlayer=null;
				}
				if(voiceRecorder!=null){
					voiceRecorder.stopRecording();
					voiceRecorder=null;
				}
			}
			
			@Override
			public void onCallDeclined(String otherPartyPseudoIdentity) {
				System.out.println("onCallDeclined");
			}
			
			
			@Override
			public void onCallAccepted(String pseudoId) {
				// TODO Auto-generated method stub
				System.out.println("onCallAccepted");
				MessageDigest md = null;
				
				try {
					md = MessageDigest.getInstance("SHA-256");
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				voiceRecorder = new VoiceRecorder(md.digest("testkey".getBytes()));
				voiceRecorder.init("198.168.1.5", "198.168.1.5", 7000);
				voiceRecorder.start();
				voicePlayer = new VoicePlayer(md.digest("testkey".getBytes()));
				voicePlayer.init("198.168.1.5", 7000);
				voicePlayer.start();
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
