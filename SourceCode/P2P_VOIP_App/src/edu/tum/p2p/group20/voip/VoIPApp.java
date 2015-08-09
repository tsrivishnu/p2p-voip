/**
 * 
 */
package edu.tum.p2p.group20.voip;

import edu.tum.p2p.group20.voip.gui.VoIPAppMainFrame;
import edu.tum.p2p.group20.voip.intraPeerCom.GoOnline;
import edu.tum.p2p.group20.voip.intraPeerCom.GoOnlineEventListener;

/**
 * @author anshulvij
 *
 */
public class VoIPApp implements GoOnlineEventListener{
	
	/**
	 * 
	 */
	public VoIPApp() {
		voipAppMainFrame = new VoIPAppMainFrame();
		voipAppMainFrame.setTitle("VoIP Application");
		voipAppMainFrame.setGoOnlineListener(this);
		voipAppMainFrame.setVisible(true);
		
	}
	
	private static VoIPAppMainFrame voipAppMainFrame;
	private static GoOnline goOnline;
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		VoIPApp app = new VoIPApp();

		
		
		
	}


	/* (non-Javadoc)
	 * @see edu.tum.p2p.group20.voip.intraPeerCom.GoOnlineEventListener#goOnline()
	 */
	@Override
	public void goOnline() {
		// TODO Auto-generated method stub
		goOnline = new GoOnline();
		
	}


	/* (non-Javadoc)
	 * @see edu.tum.p2p.group20.voip.intraPeerCom.GoOnlineEventListener#goOffline()
	 */
	@Override
	public void goOffline() {
		// TODO Auto-generated method stub
		
	}

}
