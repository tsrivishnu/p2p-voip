package edu.tum.p2p.group20.voip.voice;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.MessageDigest;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Port;
import javax.sound.sampled.TargetDataLine;

import edu.tum.p2p.group20.voip.com.MessageCrypto;
import edu.tum.p2p.group20.voip.crypto.SHA2;

public class VoiceRecorder extends Thread {

	//Destination IP
	private static final String REMOTE_IP = "192.168.1.40";
	//Port configured for voice data
	private static final int PORT = 7000;
	//TUN interface IP
	private static final String TUN_IP = "localhost";
	private DatagramSocket sock;
	private boolean stop;
	private byte[] sessionKey;
	private MessageCrypto encryptor;
	private TargetDataLine targetDataLine;
	
	/**
	 * @param sessionkey2
	 */
	public VoiceRecorder(byte[] sessionkey) {
		// TODO Auto-generated constructor stub
		sessionKey=sessionkey;
	}

	public void run() {
		try {
			MessageCrypto aes = new MessageCrypto();
			aes.setSessionKey(sessionKey);
			DataLine.Info dataLineInfo = new DataLine.Info(
					TargetDataLine.class, getAudioFormat());
			targetDataLine = (TargetDataLine) AudioSystem
					.getLine(dataLineInfo);
			targetDataLine.open(getAudioFormat());
			targetDataLine.start();
			byte tempBuffer[] = new byte[16000];

			initializeSocket();
			if(encryptor==null){
//				encryptor= new 
			}
			while (!stop) {
				targetDataLine.read(tempBuffer, 0, tempBuffer.length);
				if(!stop){
					tempBuffer = aes.encryptWithSessionKey(tempBuffer);
					sendThruUDP(tempBuffer);
				}
			}

		} catch (Exception e) {
			System.out.println(" not correct ");
			System.exit(0);
		}
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		
		// TODO Remove this testing code
		
		SHA2 sha2 = new SHA2();
		
		
		
		VoiceRecorder voiceRecorder = new VoiceRecorder(sha2.makeSHA2Hash("testkey").getBytes());
		voiceRecorder.start();

	
		

	}

	private AudioFormat getAudioFormat() {
		float sampleRate = 16000.0F;
		// 8000,11025,16000,22050,44100
		int sampleSizeInBits = 16;
		// 8,16
		int channels = 1;
		// 1,2
		boolean signed = true;
		// true,false
		boolean bigEndian = true;
		// true,false
		return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, sampleSizeInBits, 
                channels, (sampleSizeInBits/8)*channels, sampleRate, bigEndian);
	}
	
	private void initializeSocket(){
		try {
			sock = new DatagramSocket(0,InetAddress.getByName(TUN_IP));
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			System.out.println(" Unable to initialize UDP socket");
			e.printStackTrace();
		} catch (UnknownHostException e) {
			System.out.println("Cannot create UDP socket to TUN interface");
			e.printStackTrace();
		}
	}
	
	private void closeSocket(){
		if(sock!=null){
			sock.close();
			sock=null;
		}
		
	}

	public void sendThruUDP(byte soundpacket[]) {
		try {
			sock.send(new DatagramPacket(soundpacket, soundpacket.length,
					InetAddress.getByName(REMOTE_IP), PORT));
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(" Unable to send soundpacket using UDP ");
		}

	}
	
	public void stopRecording(){
		stop =true;
		if(targetDataLine!=null){
			targetDataLine.close();
			targetDataLine=null;
		}
		closeSocket();
	}

	
}