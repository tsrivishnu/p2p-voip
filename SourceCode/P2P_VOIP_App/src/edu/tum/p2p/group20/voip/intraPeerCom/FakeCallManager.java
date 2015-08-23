/**
 * 
 */
package edu.tum.p2p.group20.voip.intraPeerCom;

import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.interfaces.RSAPublicKey;
import java.sql.PseudoColumnUsage;
import java.util.Date;
import java.util.TimerTask;

import org.apache.commons.codec.binary.Base64;

import edu.tum.p2p.group20.voip.config.ConfigParser;
import edu.tum.p2p.group20.voip.crypto.RSA;
import edu.tum.p2p.group20.voip.crypto.SHA2;
import edu.tum.p2p.group20.voip.voice.CallInitiatorListener;
import edu.tum.p2p.group20.voip.voice.CallReceiverListener;
import edu.tum.p2p.group20.voip.voice.Receiver;

/**
 * Fake call works now! Will link it to the main thread if there is enough time
 * 
 * 
 * @author anshulvij
 *
 */
public class FakeCallManager extends TimerTask implements GoOnlineEventListener,
		MakeCallEventListener, CallInitiatorListener, CallReceiverListener {

	private byte[] dummyPseduoId;
	private ConfigParser configParser;
	private GoOnline goOnline;
	private MakeCall makeCall;

	/* (non-Javadoc)
	 * @see edu.tum.p2p.group20.voip.voice.CallReceiverListener#onIncomingCall(java.lang.String, byte[])
	 */
	@Override
	public boolean onIncomingCall(String pseudoId, byte[] sessionKey) {
		//accept the call
		return true;
	}

	/* (non-Javadoc)
	 * @see edu.tum.p2p.group20.voip.voice.CallReceiverListener#onDestinationIPReady(java.lang.String)
	 */
	@Override
	public void onDestinationIPReady(String destinationIP) {
		System.out.println("Fake-onDestinationIPReady");

	}

	/* (non-Javadoc)
	 * @see edu.tum.p2p.group20.voip.voice.CallReceiverListener#onIncomingCallConnected(java.lang.String, edu.tum.p2p.group20.voip.voice.Receiver, byte[])
	 */
	@Override
	public void onIncomingCallConnected(String pseudoId, Receiver receiver,
			byte[] sessionKey) {
		System.out.println("Fake-onIncomingCallConnected");

	}

	/* (non-Javadoc)
	 * @see edu.tum.p2p.group20.voip.voice.CallInitiatorListener#onCallInitiated(java.lang.String)
	 */
	@Override
	public void onCallInitiated(String pseudoId) {
		System.out.println("Fake-onCallInitiated");

	}

	/* (non-Javadoc)
	 * @see edu.tum.p2p.group20.voip.voice.CallInitiatorListener#onCallAccepted(java.lang.String, byte[], java.lang.String)
	 */
	@Override
	public void onCallAccepted(String pseudoId, byte[] sessionKey,
			String destinationIP) {
		System.out.println("Fake-onCallAccepted");

	}

	/* (non-Javadoc)
	 * @see edu.tum.p2p.group20.voip.voice.CallInitiatorListener#onCallDisconnected(java.lang.String)
	 */
	@Override
	public void onCallDisconnected(String pseudoId) {
		System.out.println("Fake-onCallDisconnected");

	}

	/* (non-Javadoc)
	 * @see edu.tum.p2p.group20.voip.voice.CallInitiatorListener#onCallDeclined(java.lang.String)
	 */
	@Override
	public void onCallDeclined(String pseudoId) {
		System.out.println("Fake-onCallDeclined");

	}

	/* (non-Javadoc)
	 * @see edu.tum.p2p.group20.voip.voice.CallInitiatorListener#onCallFailed(java.lang.String)
	 */
	@Override
	public void onCallFailed(String calleeId) {
		System.out.println("Fake-onCallFailed");

	}

	/* (non-Javadoc)
	 * @see edu.tum.p2p.group20.voip.intraPeerCom.MakeCallEventListener#onMakeCallError(java.lang.String)
	 */
	@Override
	public void onMakeCallError(String error) {
		System.out.println("Fake-onMakeCallError");

	}

	/* (non-Javadoc)
	 * @see edu.tum.p2p.group20.voip.intraPeerCom.MakeCallEventListener#onMakeCallException(java.lang.Exception)
	 */
	@Override
	public void onMakeCallException(Exception e) {
		System.out.println("Fake-onMakeCallException");

	}

	/* (non-Javadoc)
	 * @see edu.tum.p2p.group20.voip.intraPeerCom.GoOnlineEventListener#onOnline()
	 */
	@Override
	public void onOnline() {
		//make fake call to this dummy
		try {
			//waiting for 20 secs
			Thread.sleep(20000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		makeCall = new MakeCall();
		makeCall.setCallInitiatorListener(this);
		makeCall.setErrorListener(this);
		try {
			//set fake call as true
			makeCall.makeCall(Base64.encodeBase64String(dummyPseduoId), configParser,true);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/* (non-Javadoc)
	 * @see edu.tum.p2p.group20.voip.intraPeerCom.GoOnlineEventListener#onOffline()
	 */
	@Override
	public void onOffline() {
		System.out.println("Fake-onOffline");

	}

	/* (non-Javadoc)
	 * @see edu.tum.p2p.group20.voip.intraPeerCom.GoOnlineEventListener#onError(java.lang.String)
	 */
	@Override
	public void onError(String error) {
		System.out.println("Fake-onError");

	}

	/* (non-Javadoc)
	 * @see edu.tum.p2p.group20.voip.intraPeerCom.GoOnlineEventListener#onException(java.lang.Exception)
	 */
	@Override
	public void onException(Exception e) {
		System.out.println("Fake-onException");

	}

	/* (non-Javadoc)
	 * @see edu.tum.p2p.group20.voip.intraPeerCom.GoOnlineEventListener#onPseudoIdReady(java.lang.String)
	 */
	@Override
	public void onPseudoIdReady(String hostPseudoIdentity) {
		System.out.println("Fake-onPseudoIdReady");

	}

	/* (non-Javadoc)
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run() {
		
		makeFakeCall();
	}
	
	public void makeFakeCall(){
		
		try {
			System.out.println("Making fake call");
			dummyPseduoId = new SHA2().makeSHA2Hash(new Date().toString());
			KeyPair hostKeyPair = RSA.getKeyPairFromFile(configParser.getUserHostKey());
			RSAPublicKey remotePublicKey = (RSAPublicKey) hostKeyPair
					.getPublic();
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			dummyPseduoId=md.digest(remotePublicKey.getEncoded());
			goOnline = new GoOnline();
			goOnline.setCallReceiverListener(this);
			goOnline.setEventListener(this);
			//send the fakeCall param as true
			goOnline.goOnline(configParser,true);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


	/**
	 * @param configParser the configParser to set
	 */
	public void setConfigParser(ConfigParser configParser) {
		this.configParser = configParser;
	}
	
	

}
