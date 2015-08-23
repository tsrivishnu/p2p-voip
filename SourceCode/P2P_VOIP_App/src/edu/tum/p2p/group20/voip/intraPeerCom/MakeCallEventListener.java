package edu.tum.p2p.group20.voip.intraPeerCom;

public interface MakeCallEventListener {

	public void onMakeCallError(String error);
	public void onMakeCallException(Exception e);


}
