package edu.tum.p2p.group20.voip.voice;

public interface CallInitiatorListener {
	
	public void onCallInitiated(String pseudoId);
	
	public void onCallAccepted(String pseudoId, byte[] sessionKey);
	
	public void onCallDisconnected(String pseudoId);

	public void onCallDeclined(String pseudoId);

	

}
