package edu.tum.p2p.group20.voip.voice;

public interface CallInitiatorListener {
	
	public void onCallInitiated(String pseudoId);
	
	public void onCallAccepted(String pseudoId);
	
	public void onCallDisconnected(String pseudoId);

	/**
	 * @param otherPartyPseudoIdentity
	 */
	public void onCallDeclined(String otherPartyPseudoIdentity);

}
