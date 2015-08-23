/**
 * 
 */
package edu.tum.p2p.group20.voip.gui;

import java.util.Timer;

import org.apache.commons.configuration.ConfigurationException;

import edu.tum.p2p.group20.voip.config.ConfigParser;
import edu.tum.p2p.group20.voip.intraPeerCom.FakeCallManager;

/**
 * @author anshulvij
 *
 */
public class MakeFakeCallTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// TODO Auto-generated method stub
		FakeCallManager fakecallmgr= new FakeCallManager();
		try {
			fakecallmgr.setConfigParser(ConfigParser.getInstance(args[1]));
			Timer timer = new Timer();
			timer.scheduleAtFixedRate(fakecallmgr, 0, 600000);
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
