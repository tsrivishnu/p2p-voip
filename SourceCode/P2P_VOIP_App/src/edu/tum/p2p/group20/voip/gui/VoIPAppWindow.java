/**
 * 
 */
package edu.tum.p2p.group20.voip.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JLabel;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.RowSpec;

import edu.tum.p2p.group20.voip.config.ConfigParser;
import edu.tum.p2p.group20.voip.intraPeerCom.GoOnline;
import edu.tum.p2p.group20.voip.intraPeerCom.GoOnlineEventListener;
import edu.tum.p2p.group20.voip.intraPeerCom.MakeCall;
import edu.tum.p2p.group20.voip.intraPeerCom.MakeCallEventListener;
import edu.tum.p2p.group20.voip.voice.CallInitiatorListener;
import edu.tum.p2p.group20.voip.voice.CallReceiverListener;
import edu.tum.p2p.group20.voip.voice.Receiver;
import edu.tum.p2p.group20.voip.voice.VoicePlayer;
import edu.tum.p2p.group20.voip.voice.VoiceRecorder;

import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.configuration.ConfigurationException;


/**
 * @author Anshul Vij
 *
 */
public class VoIPAppWindow extends JFrame implements ActionListener,
		GoOnlineEventListener, CallReceiverListener, CallInitiatorListener, MakeCallEventListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1421485411092910846L;
	private JTextField recepientName;
	private static VoIPAppWindow appWindow;
	//can be used to save data in persistent key store manage by JRE
	@SuppressWarnings("unused")
	private PreferenceManager prefMgr;
	private GoOnline goOnlineModule;
	private MakeCall makeCallModule;
//	private Receiver receiver;
	private HashMap<String,Receiver> receiverMap;
	private JLabel lblStatusMsg;
	private VoicePlayer voicePlayer;
	private VoiceRecorder voiceRecorder;
	
	//Parser to read the config file given via cli arg
	private ConfigParser configParser;
	private byte[] sessionkey;
	private JTextField txtFieldHostPseudoId;
	private String callerPseudoId;
	private JLabel lblCallStatus;

	public VoIPAppWindow(String confiFileName) {
		try {
			configParser = ConfigParser.getInstance(confiFileName);
			System.out.println(configParser.toString());
			receiverMap = new HashMap<String, Receiver>();
		} catch (ConfigurationException e) {
			//the config File was not found
			showErrorDialog("Config file not found : "+confiFileName);
			e.printStackTrace();
			System.exit(-1);
			return;
		}
		setTitle("GROUP-20 P2P-VoIP App");
		prefMgr=PreferenceManager.getInstance();
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("40px"),
				ColumnSpec.decode("160px"),
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				ColumnSpec.decode("250px"),
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				ColumnSpec.decode("100px")},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		JLabel lblStatus = new JLabel("Status");
		panel.add(lblStatus, "2, 2, left, center");
		
		lblStatusMsg = new JLabel("Offline");
		panel.add(lblStatusMsg, "4, 2");
		
		JLabel lblCalleeName = new JLabel("Call To");
		panel.add(lblCalleeName, "2, 4");
		
		recepientName = new JTextField();
		panel.add(recepientName, "4, 4, fill, default");
		recepientName.setColumns(10);
		
		
		JButton btnCall = new JButton("Call");
		btnCall.setActionCommand("Call");
		btnCall.addActionListener(this);
		panel.add(btnCall, "6, 4");
		
		lblCallStatus = new JLabel();
		panel.add(lblCallStatus, "4, 6, left, center");
		
		JButton btnDisconnect = new JButton("Disconnect");
		btnDisconnect.setActionCommand("Disconnect");
		btnDisconnect.addActionListener(this);
		panel.add(btnDisconnect, "6, 6");
		
		JLabel lblNewLabel = new JLabel("Your Pseudo ID");
		panel.add(lblNewLabel, "2, 8");
		
		txtFieldHostPseudoId = new JTextField();
		txtFieldHostPseudoId.setEditable(false);
		panel.add(txtFieldHostPseudoId, "4, 8, fill, default");
		txtFieldHostPseudoId.setColumns(10);
		
		
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmGoOnline = new JMenuItem("Go Online");
		mntmGoOnline.setActionCommand("Go Online");
		mntmGoOnline.addActionListener(this);
		mnFile.add(mntmGoOnline);
		
		JMenuItem mntmGoOffline = new JMenuItem("Go Offline");
		mntmGoOffline.setActionCommand("Go Offline");
		mntmGoOffline.addActionListener(this);
		mnFile.add(mntmGoOffline);
		
		JMenuItem mntmQuit = new JMenuItem("Quit");
		mntmQuit.setActionCommand("Quit");
		mntmQuit.addActionListener(this);
		mnFile.add(mntmQuit);
		//sets the default window dimensions
		setBounds(100, 100, 640, 480);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}


	@Override
	public void actionPerformed(ActionEvent ae) {
		
		switch(ae.getActionCommand()){
			case "Go Online":
				if(goOnlineModule==null){
					goOnlineModule = new GoOnline();
				}
				goOnlineModule.setEventListener(this);
				try {
					//applying threading to prevent GUI from hanging
					new Thread(){
					    public void run() {
					        
					        try {
					        	goOnlineModule.goOnline(configParser);
					        } catch (Exception e) {
					        	
					            e.printStackTrace();
					        }

					    }}.start();
				} catch (Exception e) {
					System.err.println(e.getMessage());
					e.printStackTrace();
					showErrorDialog("Exception occured while trying to go online."
							+"\n"+e.getLocalizedMessage());
					lblStatusMsg.setText("Offline");
					lblStatusMsg.invalidate();
				}
				
				break;
				
			case "Go Offline":
				goOffline();
				break;
				
			case "Quit":
				shutdown();
				System.exit(0);
				break;

			case "Call" :
				String pseudoId = recepientName.getText();
				if( !"".equals(pseudoId)) {
					if( makeCallModule == null){
						makeCallModule = new MakeCall();
					}
					try {
						makeCallModule.setErrorListener(this);
						makeCallModule.setCallInitiatorListener(this);
						makeCallModule.makeCall( pseudoId,configParser);
					} catch (Exception e) {
						showErrorDialog("Could not make call due to "+e.getLocalizedMessage());
						e.printStackTrace();
					}
				}
				break;
				
			case "Disconnect" :
				
				if(makeCallModule !=null){
					makeCallModule.disconnectCall();
					makeCallModule=null;
				}
				//disconnect the received ongoing call and remove the receiver thread from map
				if(callerPseudoId!=null && receiverMap!=null && receiverMap.size()>0){
					Receiver receiver = receiverMap.get(callerPseudoId);
					if(receiver!=null){
						receiver.disconnectCall();
					}
					receiverMap.remove(callerPseudoId);
				}
				if(voicePlayer!=null){
					voicePlayer.stopSound();
					voicePlayer=null;
				}
				if(voiceRecorder!=null){
					voiceRecorder.stopRecording();
					voiceRecorder=null;
				}
				
			default:
				break;
		}
	}


	public static void main(String[] args) {

		final String[] params = args;
		if(params.length==2 && params[0].equals("-c")){
			appWindow = new VoIPAppWindow(params[1]);
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (ClassNotFoundException | InstantiationException
					| IllegalAccessException | UnsupportedLookAndFeelException e) {
				e.printStackTrace();
			}
			appWindow.setVisible(true);
		} else {
			VoIPAppWindow.showErrorDialog("Usage: java -jar VoIP_App_Group20.jar -c <ConfigFilePath>");
			System.err.println("Usage: java -jar VoIP_App_Group20.jar -c <ConfigFilePath>");
			System.exit(1);
		}
	}

	@Override
	public void onMakeCallError(String error) {
		showErrorDialog("Error while making call - "+error);
	}

	@Override
	public void onMakeCallException(Exception e) {
		showErrorDialog("Error while making call - "+e.getLocalizedMessage());
	}
	
	@Override
	public void onException(Exception e) {
		showErrorDialog("Unable to go online due to : "+e.getLocalizedMessage());
		e.printStackTrace();
		lblStatusMsg.setText("Offline");
		lblStatusMsg.invalidate();
	}
	
	@Override
	public void onError(String error) {
		showErrorDialog("Error in while trying to go online : "+error);
		lblStatusMsg.setText("Offline");
		lblStatusMsg.invalidate();
	}

	

	@Override
	public void onOnline() {
	
		lblStatusMsg.setText("Online");
		lblStatusMsg.invalidate();
		goOnlineModule.setCallReceiverListener(this);
	}
	
	@Override
	public void onOffline() {
		lblStatusMsg.setText("Offline");
		lblStatusMsg.invalidate();
	}

	@Override
	public boolean onIncomingCall(String pseudoId,byte[] sessionkey) {
		this.sessionkey=sessionkey;
		return showIncomingCallDialog(pseudoId);
	}
	/**
	 * Show a call accept yes/no dialog for user's input
	 * @param pseudoId
	 * @return true if user accepted call
	 */
	private boolean showIncomingCallDialog(String pseudoId) {
		JOptionPane optionPane = new JOptionPane(
                "Incoming call by\n"
                + pseudoId +"\n"
                +"Do you want to accept?",
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.YES_NO_OPTION);

		int dialogButton = JOptionPane.YES_NO_OPTION;
        int result = JOptionPane.showConfirmDialog (null, //parent
        		"Incoming call from "+pseudoId+"\nDo you want to accept?",//message
        		"Incoming call",//title
        		JOptionPane.YES_NO_OPTION);//buttons

		if (result == JOptionPane.YES_OPTION) {
			//call accepted
			System.out.println("User accepted call");
		    return true;
		} else if (result == JOptionPane.NO_OPTION) {
			//call declined
			System.out.println("User declined call");
		    return false;
		}
		//for all other cases call declined
		return false;
	}
	/**
	 * Callback for callee when call is connected
	 */
	@Override
	public void onIncomingCallConnected(String pseudoId, Receiver receiver, byte[]sessionKey) {
		this.callerPseudoId = pseudoId;
		receiverMap.put(pseudoId,receiver);
		//save the new session key
		this.sessionkey = sessionKey;
		//display status to user
		lblCallStatus.setText("Connected to: "+pseudoId);
		lblCallStatus.invalidate();
		
		// start listening to voice data
		voicePlayer = new VoicePlayer(sessionkey);
		voicePlayer.setCallReceiverListener(this);
		voicePlayer.init(configParser.getTunIP(), configParser.getVoipPort());
		voicePlayer.start();
	}



	@Override
	public void onCallDisconnected(String pseudoId) {
		lblCallStatus.setText("Disconnected from: "+pseudoId);
		lblCallStatus.invalidate();
		
		if(voicePlayer!=null){
			voicePlayer.stopSound();
			voicePlayer =null;
		}
		
		if(voiceRecorder!=null){
			voiceRecorder.stopRecording();
			voiceRecorder=null;
		}
	}

	@Override
	public void onCallInitiated(String pseudoId) {
		lblCallStatus.setText("Connecting to: "+pseudoId);
		lblCallStatus.invalidate();	
	}
	/**
	 * callback for caller when remote peer accepts the call
	 */
	@Override
	public void onCallAccepted(String pseudoId, byte[] sessionKey,String destinationIP) {
		lblCallStatus.setText("Connected to: "+pseudoId);
		lblCallStatus.invalidate();
		this.sessionkey=sessionKey;
		
		voicePlayer = new VoicePlayer(sessionkey);		
		voicePlayer.init(configParser.getTunIP(), configParser.getVoipPort());
		voicePlayer.start();
		
		voiceRecorder = new VoiceRecorder(sessionkey);
		voiceRecorder.init(configParser.getTunIP(), destinationIP, configParser.getVoipPort());
		voiceRecorder.start();
	}
	/**
	 * Thread safe console log message print
	 * @param s string to be printed
	 */
	private void safePrintln(String s) {
		synchronized (System.out) {
		    System.out.println(s);
		}
	}

	/* (non-Javadoc)
	 * @see edu.tum.p2p.group20.voip.voice.CallInitiatorListener#onCallDeclined(java.lang.String)
	 */
	@Override
	public void onCallDeclined(String pseudoId) {
		lblCallStatus.setText(pseudoId+" declined your call!");
		lblCallStatus.invalidate();
	}


	/* (non-Javadoc)
	 * @see edu.tum.p2p.group20.voip.intraPeerCom.GoOnlineEventListener#showPseudoID(java.lang.String)
	 */
	@Override
	public void onPseudoIdReady(String hostPseudoIdentity) {		

		txtFieldHostPseudoId.setText(hostPseudoIdentity);
		txtFieldHostPseudoId.invalidate();
	}


	/* (non-Javadoc)
	 * @see edu.tum.p2p.group20.voip.voice.CallReceiverListener#onDestinationIPReady(java.lang.String)
	 */
	@Override
	public void onDestinationIPReady(String destinationIP) {
		//Callee received first UDP packet from Caller
		//now start transmitting using the source IP
		voiceRecorder = new VoiceRecorder(sessionkey);
		voiceRecorder.init(configParser.getTunIP(), destinationIP, configParser.getVoipPort());
		voiceRecorder.start();
	}


	/* (non-Javadoc)
	 * @see edu.tum.p2p.group20.voip.voice.CallInitiatorListener#onCallFailed(java.lang.String)
	 */
	@Override
	public void onCallFailed(String calleeId) {
		lblCallStatus.setText("Unable to call "+ calleeId);
		lblCallStatus.invalidate();
	}
	
	private static void showErrorDialog(String str){
		JOptionPane.showMessageDialog(new JFrame(), str,"Error",JOptionPane.ERROR_MESSAGE);
	}
	
	private static void showMessageDialog(String str){
		JOptionPane.showMessageDialog(new JFrame(), str,"Message",JOptionPane.INFORMATION_MESSAGE);
	}
	
	private void shutdown(){
		if(makeCallModule !=null){
			makeCallModule.disconnectCall();
			makeCallModule=null;
		}
		//disconnect the received ongoing call and remove the receiver thread from map
		for(Entry entry : receiverMap.entrySet()){
			Receiver receiver = (Receiver) entry.getValue();
			String id = (String) entry.getKey();
			receiver.disconnectCall();
			receiverMap.remove(id);
		}
		if(voicePlayer!=null){
			voicePlayer.stopSound();
			voicePlayer =null;
		}
		
		if(voiceRecorder!=null){
			voiceRecorder.stopRecording();
			voiceRecorder=null;
		}
	}
	
	private void goOffline(){
		
		//disconnect the received ongoing call and remove the receiver thread from map
		for(Entry<String, Receiver> entry : receiverMap.entrySet()){
			Receiver receiver = (Receiver) entry.getValue();
			String id = (String) entry.getKey();
			receiver.disconnectCall();
			receiverMap.remove(id);
			if(voicePlayer!=null){
				voicePlayer.stopSound();
				voicePlayer =null;
			}
			
			if(voiceRecorder!=null){
				voiceRecorder.stopRecording();
				voiceRecorder=null;
			}
		}
		goOnlineModule.goOffline();
		
	}
}
