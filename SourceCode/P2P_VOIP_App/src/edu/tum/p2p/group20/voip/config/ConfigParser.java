package edu.tum.p2p.group20.voip.config;

import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalINIConfiguration;

public class ConfigParser {
	
	private static ConfigParser configParser;
	
	private String userHostKey;
	
	private int dhtPort;
	private String dhtHost;
	private List<Object> dhtOverlayHost;
	
	private int kxPort;
	private String kxhost;
	private List<Object> kxOverlayHost;
	
	private String tunIP;
	private String hostlistAddress;
	private List<String> hostList;
	
	private int voipPort;
	
	// These are used only during test!
	private String testDestinationIP; 
	private String testRemoteRsaKeyPair;
	
	private HierarchicalINIConfiguration configIni;

	private int fakeCallPort;
	/**
	 * to make class singleton
	 */
	private ConfigParser(){
		
	}
	
	public static ConfigParser getInstance(String configFileName) throws ConfigurationException{
		if(configParser==null){
			configParser= new ConfigParser();
		}
		configParser.configIni = new HierarchicalINIConfiguration(configFileName);
		if(configParser.configIni!=null){
			
			configParser.userHostKey=configParser.configIni.getSection("VOIP").getString("USER_KEYFILE",null);
			
			configParser.dhtPort=configParser.configIni.getSection("DHT").getInt("PORT");
			configParser.dhtHost=configParser.configIni.getSection("DHT").getString("HOSTNAME");
			configParser.dhtOverlayHost=configParser.configIni.getSection("DHT").getList("OVERLAY_HOSTNAME");
			configParser.hostlistAddress=configParser.configIni.getSection("DHT").getString("HOSTLIST");
			
			configParser.kxPort=configParser.configIni.getSection("KX").getInt("PORT");
			configParser.kxhost=configParser.configIni.getSection("KX").getString("HOSTNAME");
			configParser.kxOverlayHost=configParser.configIni.getSection("KX").getList("OVERLAY_HOSTNAME");
			configParser.tunIP=configParser.configIni.getSection("KX").getString("TUN_IP",null);
			
			configParser.testDestinationIP=configParser.configIni.getSection("VOIP").getString("TEST_DESTINATION_IP",null);
			configParser.testRemoteRsaKeyPair=configParser.configIni.getSection("VOIP").getString("TEST_REMOTE_RSA_KEYPAIR",null);

			configParser.voipPort = configParser.configIni.getSection("VOIP").getInt("PORT");
			configParser.fakeCallPort = configParser.configIni.getSection("VOIP").getInt("FAKE_CALL_PORT");
		}
		
		return configParser;
	}
	
	

	/**
	 * @return the hostKey
	 */
	public String getUserHostKey() {
		return userHostKey;
	}

	/**
	 * @return the dhtPort
	 */
	public int getDhtPort() {
		return dhtPort;
	}

	/**
	 * @return the dhtHost
	 */
	public String getDhtHost() {
		return dhtHost;
	}

	/**
	 * @return the dhtOverlayHost
	 */
	public List<Object> getDhtOverlayHost() {
		return dhtOverlayHost;
	}

	/**
	 * @return the kxPort
	 */
	public int getKxPort() {
		return kxPort;
	}

	/**
	 * @return the kxhost
	 */
	public String getKxhost() {
		return kxhost;
	}

	/**
	 * @return the kxOverlayHost
	 */
	public List<Object> getKxOverlayHost() {
		return kxOverlayHost;
	}

	/**
	 * @return the hostlistAddress
	 */
	public String getHostlistAddress() {
		return hostlistAddress;
	}

	/**
	 * @return the hostList
	 */
	public List<String> getHostList() {
		return hostList;
	}

	public int getVoipPort() {
		return voipPort;
	}

	public String getTunIP() {
		return tunIP;
	}
	
	/**
	 * @return
	 */
	public String getTestDestinatonIp() {
		return testDestinationIP;
	}

	/**
	 * @return
	 */
	public String getTestRemoteRsaKeyPair() {
		return testRemoteRsaKeyPair;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ConfigParser [userHostKey=" + userHostKey + ", dhtPort=" + dhtPort
				+ ", dhtHost=" + dhtHost + ", dhtOverlayHost=" + dhtOverlayHost
				+ ", kxPort=" + kxPort + ", kxhost=" + kxhost
				+ ", kxOverlayHost=" + kxOverlayHost + ", tunIP=" + tunIP
				+ ", hostlistAddress=" + hostlistAddress + ", hostList="
				+ hostList + ", voipPort=" + voipPort + ", testDestinationIP="
				+ testDestinationIP + ", testReceiverRsaKeyPair="
				+ testRemoteRsaKeyPair + ", configIni=" + configIni + "]";
	}

	/**
	 * @return
	 */
	public int getFakeCallPort() {
		// TODO Auto-generated method stub
		return fakeCallPort;
	}
}
