package edu.tum.p2p.group20.voip.voice;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class VoicePlayer extends Thread {

	private static final String IP_TO_STREAM_TO = "localhost";
	private static final int PORT_TO_STREAM_TO = 8888;
	private  DatagramSocket sock;
	private DatagramPacket datagram;
	DataLine.Info dataLineInfo;
	SourceDataLine sourceDataLine;

	/** Creates a new instance of RadioReceiver */
	public VoicePlayer() {
	}

	public void run() {
		byte b[] = null;
		initializeSocket();
		try {
			initializeSound();
			while (true) {
				b = receiveThruUDP();
				toSpeaker(b);
			}
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			System.out.println("Cannot initialize line");
			e.printStackTrace();
		}
		
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {

		VoicePlayer voicePlayer = new VoicePlayer();
		voicePlayer.start();

	}
	
	private void initializeSocket(){
		try{
			sock = new DatagramSocket(PORT_TO_STREAM_TO);
			byte soundpacket[] = new byte[16000];
			datagram = new DatagramPacket(soundpacket,
					soundpacket.length, InetAddress.getByName(IP_TO_STREAM_TO),
					PORT_TO_STREAM_TO);
		} catch(IOException ex){
			System.out.println("Cannot initialize UDP socket");
		}
	}
	
	private void closeSocket(){
		if(sock!=null){
			sock.close();
			sock=null;
		}
	}

	public  byte[] receiveThruUDP() {
		try {
			sock.receive(datagram);
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
	
	private void stopSound(){
		if(sourceDataLine!=null){
			sourceDataLine.close();
		}
		
	}
	

	public  void toSpeaker(byte soundbytes[]) {

		try {

			sourceDataLine.write(soundbytes, 0, soundbytes.length);
			sourceDataLine.drain();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("not working in speakers 2 ");
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