package edu.tum.p2p.group20.voip.voice;
import java.net.*;
import java.io.*;
import java.util.*;

// Sender is basically a client in the TCP Client-Server paradigm.
// It knows that a receiver is listening for new connections and it can send
//	and receive messages.
// To be more precise, it is the caller, who is to call a receiver.

public class Sender {
    public static void main(String[] args) throws IOException {
        
        if (args.length != 1) {
            System.err.println("Usage: java Sender <port number>");
            System.exit(1);
        }
         
        int portNumber = Integer.parseInt(args[0]);
         
        try (
            Socket socket = new Socket("127.0.0.1", Integer.parseInt(args[0]));
        	
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);                   
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        	Scanner userIn = new Scanner(System.in);
        ) {
        	String inputLine;
        	String inputFromUser;
        	
        	System.out.println(">>");
        	while((inputFromUser = userIn.nextLine()) != null){        			
	        	out.println(inputFromUser);
	        	System.out.println(">>");
        	}
        } catch (IOException e) {
            System.out.println("Exception caught when trying to connect on port " + portNumber);
            System.out.println(e.getMessage());
        }
    }
}