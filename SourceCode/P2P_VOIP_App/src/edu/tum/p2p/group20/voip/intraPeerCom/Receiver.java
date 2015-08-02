package edu.tum.p2p.group20.voip.intraPeerCom;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.crypto.tls.NewSessionTicket;

import edu.tum.p2p.group20.voip.crypto.RSA;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.dht.Put;

public class Receiver {

	private static ServerSocket serverSocket;
	private static Socket clientSocket;
	private static OutputStream out;
	private static InputStream in;
	
	public static void main(String[] args) throws NumberFormatException, UnknownHostException, IOException, NoSuchAlgorithmException, InvalidKeySpecException {

		if (args.length != 1) {
            System.err.println("Usage: java Sender <port number>");
            System.exit(1);
        }
         
        int portNumber = Integer.parseInt(args[0]);
        
        try {
        	serverSocket = new ServerSocket(portNumber);
        	clientSocket = serverSocket.accept();
	        out = clientSocket.getOutputStream();
        	in = clientSocket.getInputStream();
        		
            KeyPair hostKeyPair = RSA.getKeyPairFromFile("lib/receiver_private.pem");
        	String hostPseudoIdentity = "9caf4058012a33048ca50550e8e32285c86c8f3013091ff7ae8c5ea2519c860c";
            
        	MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        	messageDigest.update(hostPseudoIdentity.getBytes());        	
        	byte[] key = messageDigest.digest();

        	sendDhtPutMessage(key, hostKeyPair.getPublic().getEncoded());

        } catch (IOException e) {
            System.out.println("Exception caught when trying to connect on port " + portNumber);
            System.out.println(e.getMessage());
        }
	}
	
	public static void sendDhtPutMessage(byte[] key, byte[] publicKey) throws IOException {
		Put put_message = new Put(key, (short) 12, 255, publicKey);
		byte[] message = put_message.fullMessage();
		out.write(message, 0, message.length);
	}

}
