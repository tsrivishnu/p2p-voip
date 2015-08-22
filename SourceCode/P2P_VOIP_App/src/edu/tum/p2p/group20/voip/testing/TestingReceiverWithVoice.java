/**
 * 
 */
package edu.tum.p2p.group20.voip.testing;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;



















import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import edu.tum.p2p.group20.voip.config.ConfigParser;
import edu.tum.p2p.group20.voip.crypto.RSA;
import edu.tum.p2p.group20.voip.voice.CallInitiatorListener;
import edu.tum.p2p.group20.voip.voice.CallReceiverListener;
import edu.tum.p2p.group20.voip.voice.Receiver;
import edu.tum.p2p.group20.voip.voice.Sender;
import edu.tum.p2p.group20.voip.voice.VoicePlayer;
import edu.tum.p2p.group20.voip.voice.VoiceRecorder;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.ConfigurationException;

/**
 * @author anshulvij
 *
 */
public class TestingReceiverWithVoice {

	
	private static VoicePlayer voicePlayer;
	private static byte[] sessionKey;
	protected static VoiceRecorder voiceRecorder;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		
		
		ConfigParser parser;
		try {
			parser = ConfigParser.getInstance("lib/test_sample_app_config1.ini");
			ServerSocket ss = new ServerSocket(parser.getVoipPort());
			
			Socket clientSocket = ss.accept();
//			KeyPair hostKeyPair = RSA.getKeyPairFromFile("lib/receiver_private.pem");
//	    	RSAPublicKey remotePublicKey = (RSAPublicKey) hostKeyPair.getPublic();
//	    	MessageDigest md = MessageDigest.getInstance("SHA-256");
//	    	String calleeId = new String(Base64.encodeBase64(md.digest(remotePublicKey.getEncoded())));
	    	CallReceiverListener listener = new CallReceiverListener() {
				
				

				@Override
				public boolean onIncomingCall(String pseudoId, byte[] sessionKey) {
					// TODO Auto-generated method stub
					System.out.println("onIncomingCall");

		            int result = JOptionPane.showConfirmDialog (new JFrame(), "Incoming call from "+pseudoId,
		            		"Incoming Call",
		            		JOptionPane.YES_NO_OPTION);

		            if(result == JOptionPane.YES_OPTION){
		            	TestingReceiverWithVoice.sessionKey = sessionKey;
		            	return true;
		            	
		            	
		            }
		            return false;
				}
				
				@Override
				public void onCallDisconnected(String psudoId) {
					// TODO Auto-generated method stub
					System.out.println("onCallDisconnected");
				}
				
				@Override
				public void onCallConnected(String pseudoId, byte[] sessionKey) {
					// TODO Auto-generated method stub
					System.out.println("onCallConnected");
					System.out.println("sessionKey="+Base64.encodeBase64String(sessionKey));
					
				    voicePlayer = new VoicePlayer(TestingReceiverWithVoice.sessionKey);
				    voicePlayer.init("192.168.1.4", 7000);
					voicePlayer.start();
					voiceRecorder = new VoiceRecorder(TestingReceiverWithVoice.sessionKey);
					//Todo: get this IP from Makecall/sender
					voiceRecorder.init("192.168.1.4","192.168.1.5", 7000);
					voiceRecorder.start();
				}
			};
			Receiver receiver = new Receiver(clientSocket,parser,listener);
			receiver.start();
	    	
		} catch ( Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}



}
