package edu.tum.p2p.group20.voip.intraPeerCom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Sender {

	public static void main(String[] args) throws NumberFormatException, UnknownHostException, IOException {

		if (args.length != 1) {
            System.err.println("Usage: java Sender <port number>");
            System.exit(1);
        }
         
        int portNumber = Integer.parseInt(args[0]);
        
        try (
    		Socket socket = new Socket("127.0.0.1", portNumber);
        	
    		OutputStream out = socket.getOutputStream();
        	InputStream in = socket.getInputStream();
        	Scanner userIn = new Scanner(System.in);
        ) {
        	byte[] buff = new byte[2];
                        
            in.read(buff, 0, buff.length);
            System.out.println(buff);
//            out.write(buff, 0, buff.length);
//            
            short int16 = (short)(((buff[0] & 0xFF) << 8) | (buff[1] & 0xFF));
            System.out.println(int16);
        } catch (IOException e) {
            System.out.println("Exception caught when trying to connect on port " + portNumber);
            System.out.println(e.getMessage());
        }
	}

}
