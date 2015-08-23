package edu.tum.p2p.group20.voip.intraPeerCom;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

import edu.tum.p2p.group20.voip.config.ConfigParser;

public class KXSimulator {

	private static ServerSocket serverSocket;
	public static Socket socket;

	public static OutputStream out;
	public static InputStream in;
	public static Scanner userIn;
	public static String lastReceivedMessageName;
	public static byte[] lastReceivedMessage;
	public static ConfigParser parser;
	public static int portNumber;

	public static void main(String[] args) throws Exception {

		try {
			parser = ConfigParser.getInstance("lib/test_app_config.ini");

			// Make sure for testing, the DHT and KX port are same in the config
			// file!
			portNumber = parser.getKxPort();

			serverSocket = new ServerSocket(portNumber);

			socket = serverSocket.accept();
			out = socket.getOutputStream();
			in = socket.getInputStream();
			userIn = new Scanner(System.in);

			receiveMessagesLoop: while (readIncomingMessage() != null) {
				String userMessage = "What do you want to do?";
				userMessage += "\n1.Receive next message";
				userMessage += "\n2.Send IN Tunnel Ready";
				userMessage += "\n3.Send OUT Tunnel Ready";
				userMessage += "\n4.Send KX_ERROR";
				userMessage += "\n5.Send nothing";
				userMessage += "\n6.BREAK";
				System.out.println(userMessage);
				
				switch (userIn.nextLine()) {
					case "2":
						// For IN tunnel ready, destination ips can be empty
						sendTunnelReady(new byte[4], new byte[16]);
						break;
					case "3":
						// This is the destination Ip the caller uses to reach
						// callee, Since we are simulating this app, there is 
						// no real tunnel. So, the destination Ip for the
						// caller should be the ip of the receiver in our case.
						// As this is  running in a local environment, you
						// must have set the TUN_IP on the callee's machine to
						// callee's ip address. So, that becomes the
						// destinationIP.
						
						byte[] destinationIp = InetAddress.getByName(
							parser.getTunIP()
						).getAddress();

						sendTunnelReady(
							destinationIp,
							InetAddress.getByName("3ffe:2a00:100:7031::1")
								.getAddress()
						);
						
						break;
					case "4":
						sendErrorReply("MSG_KX_ERROR");
						break;
					case "6":
						break receiveMessagesLoop;
					default:
						break;
				}
			}

		} catch (IOException e) {
			System.out
					.println("Exception caught when trying to connect on port "
							+ portNumber);
			System.out.println(e.getMessage());
		}
	}

	private static byte[] readIncomingMessage() throws IOException {

		byte[] buff = new byte[2];
		// First read the length
		in.read(buff, 0, buff.length);

		short incomingSize = Helper.shortFromNetworkOrderedBytes(buff);
		incomingSize = (short) (incomingSize - 2); // Cause two bytes are
													// already read.
		byte[] incomingBytes = new byte[incomingSize];
		in.read(incomingBytes, 0, incomingSize);

		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		byteStream.write(buff);
		byteStream.write(incomingBytes);

		short messageCode = Helper.shortFromNetworkOrderedBytes(Arrays
				.copyOfRange(byteStream.toByteArray(), 2, 4));

		System.out.println("Received Message: "
				+ MessagesLegend.nameForCode(messageCode));
		lastReceivedMessageName = MessagesLegend.nameForCode(messageCode);
		lastReceivedMessage = byteStream.toByteArray();

		return lastReceivedMessage;
	}

	/**
	 * Prepends the size field for a message after calculating the size of the
	 * message and returns the full byte array of the full encapsulated message
	 * 
	 * @param message
	 * @return full message with size as byte array
	 * @throws IOException
	 */
	private static byte[] prependSizeForMessage(byte[] message)
			throws IOException {
		
		byte[] size = Helper.networkOrderedBytesFromShort(
			(short) (message.length + 2)
		); // +2 for the size of the Size field
		
		ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream();
		outputStream2.write(size);
		outputStream2.write(message);
		byte[] fullMessage = outputStream2.toByteArray();
		
		return fullMessage;
	}

	/**
	 * Sends a tunnel ready reply to the requester with the give
	 * destinationIpAddress value
	 * 
	 * @param destinationIpAddress
	 * @throws IOException
	 */
	private static void sendTunnelReady(byte[] destinationIpv4,
			byte[] destinationIpv6) throws IOException {

		byte[] key = Arrays.copyOfRange(lastReceivedMessage, 8, 40);

		byte[] messageCode = Helper
			.networkOrderedBytesFromShort(
				(short) MessagesLegend.codeForName("MSG_KX_TN_READY")
			);
		
		byte[] reserved = new byte[4];

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(messageCode);
		outputStream.write(key);
		outputStream.write(reserved);
		outputStream.write(destinationIpv4);
		outputStream.write(destinationIpv6);

		byte[] fullTunnelReadyMessage = prependSizeForMessage(outputStream
				.toByteArray());
		out.write(fullTunnelReadyMessage, 0, fullTunnelReadyMessage.length);
	}

	/**
	 * Sends an Error reply.
	 * 
	 * @param messageName
	 * @throws Exception
	 */
	private static void sendErrorReply(String messageName) throws Exception {

		byte[] key = Arrays.copyOfRange(lastReceivedMessage, 4, 36);

		byte[] messageCode = Helper
			.networkOrderedBytesFromShort(
				(short) MessagesLegend.codeForName(messageName)
			);
		byte[] reserved = new byte[2];
		byte[] request_type = Helper.networkOrderedBytesFromShort((short) 10);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(messageCode);
		outputStream.write(request_type);
		outputStream.write(reserved);
		outputStream.write(key);

		byte[] fullDhtErrorMessage = prependSizeForMessage(outputStream
				.toByteArray());
		out.write(fullDhtErrorMessage, 0, fullDhtErrorMessage.length);
	}
}
