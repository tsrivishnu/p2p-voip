package edu.tum.p2p.group20.voip.dh;

import java.security.MessageDigest;

/**
 * Example class to show the usage of the SessionKeyManager to generate 
 * common secrete session key. 
 * 
 * Usage:
 * 	Run the class and you will see the secrete key printed to the System.out 
 * 
 * @author Sri Vishnu Totakura <srivishnu@totakura.in>
 *
 */
public class ExampleDHExchange {

  public static void main(String[] args) throws Exception {    
    SessionKeyManager aSKManager = SessionKeyManager.makeInitiator();
  
    SessionKeyManager bSKManager = SessionKeyManager.makeSecondParty(aSKManager.publicByteEncodedDHKey());
    
    MessageDigest hash = MessageDigest.getInstance("SHA1");
    
    System.out.println(new String(hash.digest(bSKManager.makeSessionKey(aSKManager.publicByteEncodedDHKey()))));
    System.out.println(new String(hash.digest(aSKManager.makeSessionKey(bSKManager.publicByteEncodedDHKey()))));
  }
}