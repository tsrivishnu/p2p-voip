/**
 * 
 */
package edu.tum.p2p.group20.voip.testing;

import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.JOptionPane;

import edu.tum.p2p.group20.voip.config.ConfigParser;

import edu.tum.p2p.group20.voip.voice.CallReceiverListener;
import edu.tum.p2p.group20.voip.voice.Receiver;

import org.apache.commons.codec.binary.Base64;

/**
 * @author anshulvij
 *
 */
public class TestingReceiver {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		ConfigParser parser;
		try {
			parser = ConfigParser.getInstance("lib/test_app_config.ini");
			ServerSocket ss = new ServerSocket(parser.getVoipPort());
			
			Socket clientSocket = ss.accept();

	    	CallReceiverListener listener = new CallReceiverListener() {
				
				@Override
				public boolean onIncomingCall(String pseudoId, byte[] sessionKey) {

					System.out.println("onIncomingCall");
					int dialogButton = JOptionPane.YES_NO_OPTION;
		            int result = JOptionPane.showConfirmDialog (null, 
		            		"Incoming call from "+pseudoId,"Incoming Call",dialogButton);

		            if(result == JOptionPane.YES_OPTION){
		            	return true;
		            }
		            return false;
				}
				
				@Override
				public void onCallDisconnected(String psudoId) {
					System.out.println("onCallDisconnected");
				}
				
				@Override
				public void onIncomingCallConnected(String pseudoId, Receiver receiver, byte[] sessionKey) {
					System.out.println("onCallConnected");
					System.out.println("sessionKey = "+Base64.encodeBase64String(sessionKey));
				}

				@Override
				public void onDestinationIPReady(String destinationIP) {
					
				}
			};
			Receiver receiver = new Receiver(clientSocket,parser,listener);
			receiver.start();
	    	
		} catch ( Exception e) {
			e.printStackTrace();
		}
	}
}
