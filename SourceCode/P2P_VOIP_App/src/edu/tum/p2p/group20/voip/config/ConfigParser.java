package edu.tum.p2p.group20.voip.config;

import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalINIConfiguration;

public class ConfigParser {
	
	private static ConfigParser configParser;
	
	private String hostKey;
	
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
	private String testReceiverRsaKeyPair;
	
	private HierarchicalINIConfiguration configIni;
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
			
			configParser.hostKey=configParser.configIni.getString("HOSTKEY",null);
			
			configParser.dhtPort=configParser.configIni.getSection("DHT").getInt("PORT");
			configParser.dhtHost=configParser.configIni.getSection("DHT").getString("HOSTNAME");
			configParser.dhtOverlayHost=configParser.configIni.getSection("DHT").getList("OVERLAY_HOSTNAME");
			configParser.hostlistAddress=configParser.configIni.getSection("DHT").getString("HOSTLIST");
			
			configParser.kxPort=configParser.configIni.getSection("KX").getInt("PORT");
			configParser.kxhost=configParser.configIni.getSection("KX").getString("HOSTNAME");
			configParser.kxOverlayHost=configParser.configIni.getSection("KX").getList("OVERLAY_HOSTNAME");
			configParser.tunIP=configParser.configIni.getSection("KX").getString("TUN_IP",null);
			
			configParser.testDestinationIP=configParser.configIni.getSection("VOIP").getString("TEST_DESTINATION_IP",null);
			configParser.testReceiverRsaKeyPair=configParser.configIni.getSection("VOIP").getString("TEST_RECEIVER_RSA_KEYPAIR",null);

			configParser.voipPort = configParser.configIni.getSection("VOIP").getInt("PORT");
		}
		
		return configParser;
	}
	
	

	/**
	 * @return the hostKey
	 */
	public String getHostKey() {
		return hostKey;
	}

	/**
	 * @param hostKey the hostKey to set
	 */
	public void setHostKey(String hostKey) {
		this.hostKey = hostKey;
	}

	/**
	 * @return the dhtPort
	 */
	public int getDhtPort() {
		return dhtPort;
	}

	/**
	 * @param dhtPort the dhtPort to set
	 */
	public void setDhtPort(int dhtPort) {
		this.dhtPort = dhtPort;
	}

	/**
	 * @return the dhtHost
	 */
	public String getDhtHost() {
		return dhtHost;
	}

	/**
	 * @param dhtHost the dhtHost to set
	 */
	public void setDhtHost(String dhtHost) {
		this.dhtHost = dhtHost;
	}

	/**
	 * @return the dhtOverlayHost
	 */
	public List<Object> getDhtOverlayHost() {
		return dhtOverlayHost;
	}

	/**
	 * @param dhtOverlayHost the dhtOverlayHost to set
	 */
	public void setDhtOverlayHost(List<Object> dhtOverlayHost) {
		this.dhtOverlayHost = dhtOverlayHost;
	}

	/**
	 * @return the kxPort
	 */
	public int getKxPort() {
		return kxPort;
	}

	/**
	 * @param kxPort the kxPort to set
	 */
	public void setKxPort(int kxPort) {
		this.kxPort = kxPort;
	}

	/**
	 * @return the kxhost
	 */
	public String getKxhost() {
		return kxhost;
	}

	/**
	 * @param kxhost the kxhost to set
	 */
	public void setKxhost(String kxhost) {
		this.kxhost = kxhost;
	}

	/**
	 * @return the kxOverlayHost
	 */
	public List<Object> getKxOverlayHost() {
		return kxOverlayHost;
	}

	/**
	 * @param kxOverlayHost the kxOverlayHost to set
	 */
	public void setKxOverlayHost(List<Object> kxOverlayHost) {
		this.kxOverlayHost = kxOverlayHost;
	}

	/**
	 * @return the hostlistAddress
	 */
	public String getHostlistAddress() {
		return hostlistAddress;
	}

	/**
	 * @param hostlistAddress the hostlistAddress to set
	 */
	public void setHostlistAddress(String hostlistAddress) {
		this.hostlistAddress = hostlistAddress;
	}

	/**
	 * @return the hostList
	 */
	public List<String> getHostList() {
		return hostList;
	}

	/**
	 * @param hostList the hostList to set
	 */
	public void setHostList(List<String> hostList) {
		this.hostList = hostList;
	}



	public int getVoipPort() {
		return voipPort;
	}

	public void setVoipPort(int port) {
		this.voipPort = port;
	}

	public String getTunIP() {
		return tunIP;
	}

	public void setTunIP(String tunIP) {
		this.tunIP = tunIP;
	}
	
	/**
	 * @return
	 */
	public String getTestDestinatonIp() {
		// TODO Auto-generated method stub
		return testDestinationIP;
	}

	

	/**
	 * @return
	 */
	public String getTestReceiverRsaKeyPair() {
		// TODO Auto-generated method stub
		return testReceiverRsaKeyPair;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ConfigParser [hostKey=" + hostKey + ", dhtPort=" + dhtPort
				+ ", dhtHost=" + dhtHost + ", dhtOverlayHost=" + dhtOverlayHost
				+ ", kxPort=" + kxPort + ", kxhost=" + kxhost
				+ ", kxOverlayHost=" + kxOverlayHost + ", tunIP=" + tunIP
				+ ", hostlistAddress=" + hostlistAddress + ", hostList="
				+ hostList + ", voipPort=" + voipPort + ", testDestinationIP="
				+ testDestinationIP + ", testReceiverRsaKeyPair="
				+ testReceiverRsaKeyPair + ", configIni=" + configIni + "]";
	}

	

}
