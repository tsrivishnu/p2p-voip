package edu.tum.p2p.group20.voip.voice;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.NoSuchPaddingException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import edu.tum.p2p.group20.voip.com.MessageCrypto;
import edu.tum.p2p.group20.voip.config.ConfigParser;
import edu.tum.p2p.group20.voip.crypto.SHA2;


public class VoicePlayer extends Thread {

	
	//Port configured for voice data
	private int port = 7000;
	//TUN interface IP
	private String tunIP = "192.168.1.5";
	
	public void init(String tunIP, int port){
		this.tunIP=tunIP;
		
		this.port=port;
		
	}
	
	

	/**
	 * @return the destinationPort
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param destinationPort the destinationPort to set
	 */
	public void setPort(int port) {
		this.port = port;
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

	//Socket for receiving UDP voice data packet
	private  DatagramSocket sock;
	//Packet that will contain actual data
	private DatagramPacket datagram;
	//Audio playback line
	DataLine.Info dataLineInfo;
	SourceDataLine sourceDataLine;
	
	//Flag to stop this thread
	private boolean stop;
	private byte[] sessionkey;
//	private ConfigParser configParser;
	
	/**
	 * @param sessionkey
	 */
	public VoicePlayer(byte[] sessionkey/*,ConfigParser parser*/) {
		this.sessionkey = sessionkey;
//		configParser = parser;
		
	}

	@Override
	/**
	 * Overridden method that is called when this start() method is invoked for this thread
	 */
	public void run() {
		byte plainData[] = null;
		MessageCrypto aes = new MessageCrypto();
		try {
			//create aes encryption manager with noPadding option
			aes.setSessionKey(sessionkey,true);
		} catch (NoSuchAlgorithmException | NoSuchProviderException
				| NoSuchPaddingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			//Display error to user although its our problem
			return;
		}
		initializeSocket();
		try {
			initializeSound();
			while (!stop) {
				plainData = receiveThruUDP();
				
				toSpeaker(aes.decryptToBytesWithSessionKey(plainData));
			}
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			System.err.println("Cannot initialize line");
			e.printStackTrace();
		}
		
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		SHA2 sha2 = new SHA2();
		
		MessageDigest md = null;
		
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		VoicePlayer voicePlayer = new VoicePlayer(md.digest("testkey".getBytes()));
		voicePlayer.start();

	}
	
	private void initializeSocket(){
		try{
			System.out.println("initializing player socket");
			System.out.println("port="+port);
			System.out.println("tunIP="+tunIP);
			sock = new DatagramSocket(port ,InetAddress.getByName(tunIP));
			byte soundpacket[] = new byte[16000];
			datagram = new DatagramPacket(soundpacket,
					soundpacket.length);
		} catch(IOException e){
			System.out.println("Cannot initialize UDP socket");
			e.printStackTrace();
		}
	}
	
	private void closeSocket(){
		if(sock!=null){
			sock.close();
			sock=null;
		}
	}

	private  byte[] receiveThruUDP() {
		try {
			System.out.println("Trying to receive a packet");
			sock.receive(datagram);
			System.out.println("Received packet");
			return datagram.getData(); // soundpacket ;
		} catch (Exception e) {
			System.out.println(" Unable to receive using UDP ");
			return null;
		}

	}
	
	private void initializeSound() throws LineUnavailableException{

			dataLineInfo = new DataLine.Info(
					SourceDataLine.class, getAudioFormat());
			sourceDataLine = (SourceDataLine) AudioSystem
					.getLine(dataLineInfo);
			sourceDataLine.open(getAudioFormat());
			sourceDataLine.start();
		
	}
	
	public void stopSound(){
		stop =true;
		if(sourceDataLine!=null){
			sourceDataLine.close();
			sourceDataLine=null;
		}
		closeSocket();
		
	}
	

	public  void toSpeaker(byte soundbytes[]) {

		try {

			sourceDataLine.write(soundbytes, 0, soundbytes.length);
			sourceDataLine.drain();
		} catch (Exception e) {
			System.out.println("Exception while playback of received audio");
			e.printStackTrace();
		}

	}

	public  AudioFormat getAudioFormat() {
		float sampleRate = 16000.0F;
		// 8000,11025,16000,22050,44100
		int sampleSizeInBits = 16;
		// 8,16
		int channels = 1;
		// 1,2
		boolean bigEndian = true;
		// true,false
		return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, sampleSizeInBits, 
                channels, (sampleSizeInBits/8)*channels, sampleRate, bigEndian);
	}

}