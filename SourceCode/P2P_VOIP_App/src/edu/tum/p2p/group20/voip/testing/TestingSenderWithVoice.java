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
	private static ConfigParser parser = null;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
		try {
			parser = ConfigParser.getInstance("lib/test_app_config.ini");
		} catch (ConfigurationException e1) {
			e1.printStackTrace();
		}
		
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
			public void onCallAccepted(String pseudoId,byte[] sessionKey,String destinationIP) {

				System.out.println("sessionKey="+Base64.encodeBase64String(sessionKey));
				voicePlayer = new VoicePlayer(sessionKey);
				voicePlayer.init(parser.getTunIP(), 7000);
				voicePlayer.start();
				
				voiceRecorder = new VoiceRecorder(sessionKey);
				voiceRecorder.init(parser.getTunIP(), destinationIP, 7000);
				voiceRecorder.start();
	
			}
			
			@Override
			public void onCallFailed(String calleeId) {
				System.out.println("onCallFailed");
			}
		});
		
		try {
			KeyPair hostKeyPair = RSA.getKeyPairFromFile(parser.getHostKey());
	    	RSAPublicKey remotePublicKey = (RSAPublicKey) hostKeyPair.getPublic();
	    	MessageDigest md = MessageDigest.getInstance("SHA-256");
	    	String receiverPseudoId = Base64.encodeBase64String(md.digest(remotePublicKey.getEncoded()));
	    	//TOOD: get this IP from TUN_READY destination IP
			sender.initiateCall(receiverPseudoId, remotePublicKey,parser.getTestDestinatonIp(), parser);
		} catch ( Exception e) {
			e.printStackTrace();
		}
		
	}



}
