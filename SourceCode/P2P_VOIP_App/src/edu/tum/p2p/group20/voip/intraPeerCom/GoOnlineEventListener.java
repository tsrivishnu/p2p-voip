package edu.tum.p2p.group20.voip.intraPeerCom;

public interface GoOnlineEventListener {
	public void onConnected();
	public void onDisonnected();
	public void onError(String error);
	public void onException(Exception e);

}
