package edu.tum.p2p.group20.voip.voice;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
	private String destinationIP = "192.168.1.40";//only for testing
	//Port configured for voice data
	private int destinationPort = 7000;//only for testing
	//TUN interface IP
	private String tunIP = "192.168.1.5";//only for testing
	
	private DatagramSocket sock;
	private boolean stop;
	private byte[] sessionKey;
	private MessageCrypto encryptor;
	private TargetDataLine targetDataLine;
	public void init(String tunIP, String destinationIP, int destinationPort){
		this.tunIP=tunIP;
		this.destinationIP=destinationIP;
		this.destinationPort=destinationPort;
		
	}
	
	/**
	 * @return the destinationIP
	 */
	public String getDestinationIP() {
		return destinationIP;
	}

	/**
	 * @param destinationIP the destinationIP to set
	 */
	public void setDestinationIP(String destinationIP) {
		this.destinationIP = destinationIP;
	}

	/**
	 * @return the destinationPort
	 */
	public int getDestinationPort() {
		return destinationPort;
	}

	/**
	 * @param destinationPort the destinationPort to set
	 */
	public void setDestinationPort(int destinationPort) {
		this.destinationPort = destinationPort;
	}

	/**
	 * @return the tunIP
	 */
	public String getTunIP() {
		return tunIP;
	}

	/**
	 * @param tunIP the tunIP to set
	 */
	public void setTunIP(String tunIP) {
		this.tunIP = tunIP;
	}
	

	
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
			aes.setSessionKey(sessionKey,true);
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
			e.printStackTrace();
			this.interrupt();
		}
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		
		// TODO Remove this testing code
		
		MessageDigest md = null;
		
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		VoiceRecorder voiceRecorder = new VoiceRecorder(md.digest("testkey".getBytes()));
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
			//can send via any port on TUN interface
			System.out.println("initializing recorder socket");
			System.out.println("port="+0);
			System.out.println("tunIP="+tunIP);
			sock = new DatagramSocket(0,InetAddress.getByName(tunIP));
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
			System.out.println("destinationIP"+destinationIP);
			System.out.println("destinationPort"+destinationPort);
			sock.send(new DatagramPacket(soundpacket, soundpacket.length,
					InetAddress.getByName(destinationIP), destinationPort));
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