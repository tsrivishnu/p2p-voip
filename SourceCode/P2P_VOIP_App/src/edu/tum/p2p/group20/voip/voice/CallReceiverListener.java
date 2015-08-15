package edu.tum.p2p.group20.voip.voice;

public interface CallReceiverListener {
	
	public boolean onIncomingCall(String pseudoId);
	
	public void onCallConnected(String pseudoId);
	
	public void onCallDisconnected(String psudoId);

}
