package edu.tum.p2p.group20.voip.voice;

public interface CallReceiverListener {
	
	public boolean onIncomingCall(String pseudoId, byte[] sessionKey);
	
	
	public void onCallDisconnected(String psudoId);

	public void onDestinationIPReady(String destinationIP);

	/**
	 * @param pseudoId of the caller
	 * @param receiver the Thread handle that controls this call
	 * @param sessionKey the session key used to encrypt the messages and data for this call
	 */
	void onIncomingCallConnected(String pseudoId, Receiver receiver, byte[] sessionKey);
}
