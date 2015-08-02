package edu.tum.p2p.group20.voip.intraPeerCom;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Scanner;

public class KXSimulator {
	
	public static Socket socket;
	
	public static OutputStream out;
	public static InputStream in;
	public static Scanner userIn;

	public static void main(String[] args) throws NumberFormatException, UnknownHostException, IOException {

		if (args.length != 1) {
            System.err.println("Usage: java Sender <port number>");
            System.exit(1);
        }
         
        int portNumber = Integer.parseInt(args[0]);
        
        try {
        	socket = new Socket("127.0.0.1", portNumber);
        	
    		out = socket.getOutputStream();
        	in = socket.getInputStream();
        	userIn = new Scanner(System.in);        	
        	
        	byte[] receivedMessage = readIncomingMessage();
        	
            System.out.println(Arrays.toString(receivedMessage));
//            out.write(buff, 0, buff.length);

        } catch (IOException e) {
            System.out.println("Exception caught when trying to connect on port " + portNumber);
            System.out.println(e.getMessage());
        }
	}
	
	private static byte[] readIncomingMessage() throws IOException {
		byte[] buff = new byte[2];
    	// First read the length
        in.read(buff, 0, buff.length);
        
        short incomingSize = Helper.shortFromNetworkOrderedBytes(buff);
        incomingSize = (short) (incomingSize - 2); // Cause two bytes are already read.
        byte[] incomingBytes = new byte[incomingSize];
        in.read(incomingBytes, 0, incomingSize);
        
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        byteStream.write(buff);
        byteStream.write(incomingBytes);
		
        return byteStream.toByteArray();
	}

}
