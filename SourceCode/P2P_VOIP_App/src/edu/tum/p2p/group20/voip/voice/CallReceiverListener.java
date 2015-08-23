package edu.tum.p2p.group20.voip.voice;

public interface CallReceiverListener {
	
	public boolean onIncomingCall(String pseudoId, byte[] sessionKey);
	
	public void onCallConnected(String pseudoId, byte[] sessionKey);
	
	public void onCallDisconnected(String psudoId);

	public void onDestinationIPReady(String destinationIP);
}
