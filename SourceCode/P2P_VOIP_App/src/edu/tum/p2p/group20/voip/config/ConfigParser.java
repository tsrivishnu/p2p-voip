package edu.tum.p2p.group20.voip.config;

import org.apache.commons.configuration.HierarchicalINIConfiguration;

public class ConfigParser {

	public static void main(String[] args) throws Exception{
		// TODO This filename has to be from the user, either via command line
		//  	or the GUI.
	    String fileName = "lib/sample_app_config";

    	HierarchicalINIConfiguration configIni = new HierarchicalINIConfiguration(fileName);
    	
    	System.out.println(configIni.getString("HOSTKEY",null));
    	System.out.println(configIni.getSection("DHT").getString("PORT",null));
    	System.out.println(configIni.getSection("DHT").getList("OVERLAY_HOSTNAME").get(1).toString().trim());
    	System.out.println(configIni.getSection("KX").getString("TUN_IP",null));
	}

}
