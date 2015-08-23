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
import edu.tum.p2p.group20.voip.voice.VoicePlayer;
import edu.tum.p2p.group20.voip.voice.VoiceRecorder;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.ConfigurationException;

/**
 * This class is used to test the call control protocol for a caller.
 * WARNING: This class is to be used only in connection with TestingReceiverWithVoice
 * running on a different PC and their corresponding config file should be located in
 * "test/test_app_config.ini" folder and have other's system IP as TEST_DESTINATION_IP
 * and other user's RSA key pair (we only extract the public key from that)
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
			parser = ConfigParser.getInstance("test/test_app_config.ini");
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
				if (voicePlayer != null) {
					voicePlayer.stopSound();
					voicePlayer = null;
				}
				if (voiceRecorder != null) {
					voiceRecorder.stopRecording();
					voiceRecorder = null;
				}
			}

			@Override
			public void onCallDeclined(String otherPartyPseudoIdentity) {
				System.out.println("onCallDeclined");
			}

			@Override
			public void onCallAccepted(String pseudoId, byte[] sessionKey,
					String destinationIP) {

				System.out.println("sessionKey="
						+ Base64.encodeBase64String(sessionKey));
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
			KeyPair hostKeyPair = RSA.getKeyPairFromFile(parser.getUserHostKey());
			RSAPublicKey remotePublicKey = (RSAPublicKey) hostKeyPair
					.getPublic();
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			String receiverPseudoId = Base64.encodeBase64String(md
					.digest(remotePublicKey.getEncoded()));
			// TOOD: get this IP from TUN_READY destination IP
			sender.initiateCall(receiverPseudoId, remotePublicKey,
					parser.getTestDestinatonIp(), parser,false);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
