/**
 * 
 */
package edu.tum.p2p.group20.voip.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.util.HashMap;

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
import edu.tum.p2p.group20.voip.testing.TestingReceiverWithVoice;
import edu.tum.p2p.group20.voip.voice.CallInitiatorListener;
import edu.tum.p2p.group20.voip.voice.CallReceiverListener;
import edu.tum.p2p.group20.voip.voice.Receiver;
import edu.tum.p2p.group20.voip.voice.Sender;
import edu.tum.p2p.group20.voip.voice.VoicePlayer;
import edu.tum.p2p.group20.voip.voice.VoiceRecorder;

import javax.swing.JDialog;
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
		GoOnlineEventListener, CallReceiverListener, CallInitiatorListener {
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
	private String destinationIP;
	private String callerPseudoId;
	

	
	public VoIPAppWindow(String confiFileName) {
		try {
			configParser = ConfigParser.getInstance(confiFileName);
			System.out.println(configParser.toString());
			receiverMap = new HashMap<String, Receiver>();
		} catch (ConfigurationException e) {
			//the config File was not found
			e.printStackTrace();
			
			System.exit(-1);
			return;
		}
		setTitle("GROUP-20 P2P-VoIP App");
		prefMgr=PreferenceManager.getInstance();
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("116px"),
				ColumnSpec.decode("31px"),
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				ColumnSpec.decode("79px:grow"),
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				ColumnSpec.decode("81px"),},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("23px"),
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
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		JLabel lblStatus = new JLabel("Status");
		panel.add(lblStatus, "1, 2, left, center");
		
		lblStatusMsg = new JLabel("Status Message");
		panel.add(lblStatusMsg, "4, 2");
		
		JLabel lblCalleeName = new JLabel("Call To");
		panel.add(lblCalleeName, "1, 6");
		
		recepientName = new JTextField();
		panel.add(recepientName, "4, 6, fill, default");
		recepientName.setColumns(10);
		
		JButton btnCall = new JButton("Call");
		btnCall.setActionCommand("Call");
		btnCall.addActionListener(this);
		panel.add(btnCall, "6, 6");
		
		JButton btnDisconnect = new JButton("Disconnect");
		btnDisconnect.setActionCommand("Disconnect");
		btnDisconnect.addActionListener(this);
		
		panel.add(btnDisconnect, "6, 8");
		
		JLabel lblNewLabel = new JLabel("Your Pseudo ID");
		panel.add(lblNewLabel, "1, 10");
		
		txtFieldHostPseudoId = new JTextField();
		txtFieldHostPseudoId.setEditable(false);
		panel.add(txtFieldHostPseudoId, "4, 10, fill, default");
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
		
		JMenu mnSettings = new JMenu("Settings");
		menuBar.add(mnSettings);
		
		JMenuItem mntmSettings = new JMenuItem("Network Settings");
		mntmSettings.setActionCommand("Network Settings");
		mntmSettings.addActionListener(this);
		mnSettings.add(mntmSettings);
		setBounds(0, 0, 640, 480);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		
	}


	@Override
	public void actionPerformed(ActionEvent ae) {
		
		switch(ae.getActionCommand()){
			case "Go Online":
				if(goOnlineModule==null){
					goOnlineModule = new GoOnline();
					goOnlineModule.setEventListener(this);
					//Todo: get this port number from settings
					try {
						boolean result = goOnlineModule.goOnline(configParser);
//						if(result){
//							lblStatusMsg.setText("You are Online now!");
//						} else {
//							lblStatusMsg.setText("Offline!");
//						}
//						lblStatusMsg.invalidate();
					} catch (Exception e) {
						System.err.println(e.getMessage());
						e.printStackTrace();
						lblStatusMsg.setText("Error occured while trying to go online!");
						lblStatusMsg.invalidate();
					}
				}
				
				break;
			case "Go Offline":
				//TODO:disconnect any calls
				//TODO: offline
				
				break;
			case "Quit":
				//TODO:disconnect any calls
				//TODO:go offline
				System.exit(0);
				break;
			case "Network Settings":
				//TODO: Create a network setting screen
				break;

				
			case "Call" :
				String pseudoId = recepientName.getText();
				if(makeCallModule ==null){
					makeCallModule = new MakeCall();
				}
				try {
					makeCallModule.setCallInitiatorListener(this);
					makeCallModule.makeCall( pseudoId,configParser);
				} catch (Exception e) {
					// TODO Show dialog to user about the error
					e.printStackTrace();
				}
				break;
				
			case "Disconnect" :
				
				if(makeCallModule !=null){
					makeCallModule.disconnectCall();
					makeCallModule=null;
				}
				//disconnect the received connected call
				if(callerPseudoId!=null &&receiverMap!=null && receiverMap.size()>0){
					Receiver receiver = receiverMap.get(callerPseudoId);
					if(receiver!=null){
						receiver.disconnectCall();
					}
					receiverMap.remove(callerPseudoId);
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
			System.err.println("Usage: java -jar VoIPAppWindow -c <ConfigFilePath>");
			System.exit(1);
		}
	}

	@Override
	public void onError(String error) {

		lblStatusMsg.setText("Error in GoOnline -"+error);
		lblStatusMsg.invalidate();
	}

	@Override
	public void onException(Exception e) {

		lblStatusMsg.setText("Unhandled Exception in GoOnline");
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
		lblStatusMsg.setText("Connected to: "+pseudoId);
		lblStatusMsg.invalidate();
		
		// start listening to voice data
		voicePlayer = new VoicePlayer(sessionkey);
		voicePlayer.setCallReceiverListener(this);
		voicePlayer.init(configParser.getTunIP(), configParser.getVoipPort());
		voicePlayer.start();
//		
//		// start transmitting voice data
//		voiceRecorder = new VoiceRecorder(sessionkey);
//		voiceRecorder.init(configParser.getTunIP(), configParser.getTestDestinatonIp(), configParser.getVoipPort());
//		voiceRecorder.start();
	}



	@Override
	public void onCallDisconnected(String pseudoId) {
		lblStatusMsg.setText("Disconnected from: "+pseudoId);
		lblStatusMsg.invalidate();
		
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
		lblStatusMsg.setText("Connecting to: "+pseudoId);
		lblStatusMsg.invalidate();	
	}
	/**
	 * callback for caller when remote peer accepts the call
	 */
	@Override
	public void onCallAccepted(String pseudoId, byte[] sessionKey,String destinationIP) {
		lblStatusMsg.setText("Connected to: "+pseudoId);
		lblStatusMsg.invalidate();
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
	public void safePrintln(String s) {
		synchronized (System.out) {
		    System.out.println(s);
		}
	}

	/* (non-Javadoc)
	 * @see edu.tum.p2p.group20.voip.voice.CallInitiatorListener#onCallDeclined(java.lang.String)
	 */
	@Override
	public void onCallDeclined(String pseudoId) {
		lblStatusMsg.setText(pseudoId+" declined your call!");
		lblStatusMsg.invalidate();
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
		this.destinationIP = destinationIP;
		voiceRecorder = new VoiceRecorder(sessionkey);
		voiceRecorder.init(configParser.getTunIP(), destinationIP, configParser.getVoipPort());
		voiceRecorder.start();
	}


	
	
}
