package edu.tum.p2p.group20.voip.intraPeerCom;

public interface GoOnlineEventListener {
	public void onOnline();
	public void onOffline();
	public void onError(String error);
	public void onException(Exception e);

}
