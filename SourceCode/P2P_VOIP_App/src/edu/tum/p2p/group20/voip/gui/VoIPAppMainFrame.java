/**
 * 
 */
package edu.tum.p2p.group20.voip.gui;

import java.awt.Button;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;

import edu.tum.p2p.group20.voip.intraPeerCom.GoOnlineEventListener;

/**
 * @author anshulvij
 *
 */
public class VoIPAppMainFrame extends JFrame implements ActionListener{

	private Button goOnlineBtn;
	private GoOnlineEventListener goOnlineEventListener;
	/**
	 * 
	 */
	public VoIPAppMainFrame() {
		frameInit();
		setLayout(new CardLayout());
		setBounds(0, 0, 640, 480);
		goOnlineBtn = new Button("Go Online");
		add(goOnlineBtn);
		goOnlineBtn.addActionListener(this);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(goOnlineBtn!=null && goOnlineBtn ==  e.getSource()){
			goOnlineEventListener.goOnline();
		}
	}

	/**
	 * @param voIPApp
	 */
	public void setGoOnlineListener(GoOnlineEventListener listener) {
		// TODO Auto-generated method stub
		goOnlineEventListener = listener;
	}
}
