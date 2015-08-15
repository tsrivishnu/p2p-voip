package edu.tum.p2p.group20.voip.voice;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Port;
import javax.sound.sampled.TargetDataLine;

import edu.tum.p2p.group20.voip.com.MessageCrypto;

public class VoiceRecorder extends Thread {

	private static final String TUN_IP = "localhost";
	private static final String DESTINATION_IP = "localhost";
	private static final int PORT = 7000;
	private DatagramSocket sock;
	private boolean stop;
	private String sessionKey;
	private MessageCrypto encryptor;
	private TargetDataLine targetDataLine;
	
	public void run() {
		try {

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
					//TODO:encrypt the packet with session key
					//TODO:what about signing
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
		VoiceRecorder voiceRecorder = new VoiceRecorder();
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
			sock = new DatagramSocket(7000,InetAddress.getByName(TUN_IP));
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
					InetAddress.getByName(DESTINATION_IP), PORT));
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

	public String getSessionKey() {
		return sessionKey;
	}

	public void setSessionKey(String sessionKey) {
		this.sessionKey = sessionKey;
	}

}