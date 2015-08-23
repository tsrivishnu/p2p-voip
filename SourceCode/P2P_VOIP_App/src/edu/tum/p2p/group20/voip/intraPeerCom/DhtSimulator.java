package edu.tum.p2p.group20.voip.intraPeerCom;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Scanner;

import edu.tum.p2p.group20.voip.config.ConfigParser;
import edu.tum.p2p.group20.voip.crypto.RSA;
import edu.tum.p2p.group20.voip.intraPeerCom.Helper;
import edu.tum.p2p.group20.voip.intraPeerCom.MessagesLegend;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.dht.Put;

/**
 * Simulates the DHT module. Used for testing
 * 
 * @author Sri Vishnu Totakura <srivishnu@totakura.in>
 *
 */
public class DhtSimulator {

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
			portNumber = parser.getDhtPort();

			serverSocket = new ServerSocket(portNumber);

			socket = serverSocket.accept();
			out = socket.getOutputStream();
			in = socket.getInputStream();
			userIn = new Scanner(System.in);

			receiveMessagesLoop: while (readIncomingMessage() != null) {
				String userMessage = "What do you want to do?";
				userMessage += "\n1.Receive next message";
				userMessage += "\n2.Send DHT_TRACE_REPLY";
				userMessage += "\n3.Send DHT_GET_REPLY";
				userMessage += "\n4.Send DHT_ERROR";
				userMessage += "\n5.Send nothing";
				userMessage += "\n6.BREAK";

				System.out.println(userMessage);

				switch (userIn.nextLine()) {
					case "2":
						sendDhtTraceReply();
						break;
					case "3":
						sendDhtDummyGetReply();
						break;
					case "4":
						sendErrorReply("MSG_DHT_ERROR");
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

		short messageCode = Helper.shortFromNetworkOrderedBytes(
			Arrays.copyOfRange(byteStream.toByteArray(), 2, 4)
		);

		System.out.println("Received Message: "
			+ MessagesLegend.nameForCode(messageCode));
		lastReceivedMessageName = MessagesLegend.nameForCode(messageCode);
		lastReceivedMessage = byteStream.toByteArray();

		return lastReceivedMessage;
	}

	/**
	 * Send a Dummy DHT GET REPLY for the received DHT GET message. This is only
	 * a Dummy, the content in this makes no sense.
	 * 
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws SignatureException
	 * @throws InvalidKeyException
	 */
	private static void sendDhtDummyGetReply() throws IOException,
		NoSuchAlgorithmException, InvalidKeyException, SignatureException {

		byte[] pseudoId = Arrays.copyOfRange(lastReceivedMessage, 4, 36);		
		byte[] messageCode = Helper.networkOrderedBytesFromShort(
			(short) MessagesLegend.codeForName("MSG_DHT_GET_REPLY")
		);

		// xChangePointInfo
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update("exchangePoint2".getBytes());
		byte[] peer2Id = md.digest();
		byte[] peer2KxPort = Helper.networkOrderedBytesFromShort((short) 3001);
		byte[] peer2reserved = new byte[2];
		byte[] peer2ip = InetAddress.getByName("192.168.2.2").getAddress();
		byte[] peer2ipv6 = InetAddress
							.getByName("3ffe:2a00:100:7031::1")
							.getAddress();

		ByteArrayOutputStream xchangeStream = new ByteArrayOutputStream();
		xchangeStream.write(peer2KxPort);
		xchangeStream.write(peer2reserved);
		xchangeStream.write(peer2Id);
		xchangeStream.write(peer2ip);
		xchangeStream.write(peer2ipv6);
		byte[] xchangePointInfo = xchangeStream.toByteArray();

		KeyPair hostKeyPair = RSA.getKeyPairFromFile(parser.getHostKey());

		// We use a sample put to get all the signature and proper content
		// encapsulation
		Put samplePut = new Put(
			pseudoId,
			(short) 1000,
			2,
			hostKeyPair,
			xchangePointInfo
		);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(messageCode);
		outputStream.write(pseudoId);
		// content
		outputStream.write(samplePut.byteValues.get("publicKey"));
		outputStream.write(samplePut.byteValues.get("pseudoIdToBeSigned"));
		outputStream.write(samplePut.byteValues.get("xchangePointInfoForKx"));
		outputStream.write(samplePut.byteValues.get("signature"));

		byte[] fullDhtReplyMessage = prependSizeForMessage(
			outputStream.toByteArray()
		);
		out.write(fullDhtReplyMessage, 0, fullDhtReplyMessage.length);
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
	 * Send a Sample DHT TRACE REPLY message based on the last message received
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	private static void sendDhtTraceReply() throws NoSuchAlgorithmException,
		IOException {

		byte[] key = Arrays.copyOfRange(lastReceivedMessage, 4, 36);

		byte[] messageCode = Helper.networkOrderedBytesFromShort(
			(short) MessagesLegend.codeForName("MSG_DHT_TRACE_REPLY")
		);

		MessageDigest md = MessageDigest.getInstance("SHA-256");

		byte[] peer1Id = md.digest("exchangePoint1".getBytes());
		byte[] peer1KxPort = Helper.networkOrderedBytesFromShort((short) 3001);
		byte[] peer1reserved = new byte[2];
		byte[] peer1ip = InetAddress.getByName("192.168.2.1").getAddress();
		byte[] peer1ipv6 = InetAddress
							.getByName("FEDC:BA98:7654:3210:FEDC:BA98:7654:3210")
							.getAddress();

		byte[] peer2Id = md.digest("exchangePoint2".getBytes());
		byte[] peer2KxPort = Helper.networkOrderedBytesFromShort((short) 3001);
		byte[] peer2reserved = new byte[2];
		byte[] peer2ip = InetAddress.getByName("192.168.2.2").getAddress();
		byte[] peer2ipv6 = InetAddress
							.getByName("3ffe:2a00:100:7031::1")
							.getAddress();

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(messageCode);
		outputStream.write(key);

		outputStream.write(peer2Id);
		outputStream.write(peer2KxPort);
		outputStream.write(peer2reserved);
		outputStream.write(peer2ip);
		outputStream.write(peer2ipv6);

		outputStream.write(peer1Id);
		outputStream.write(peer1KxPort);
		outputStream.write(peer1reserved);
		outputStream.write(peer1ip);
		outputStream.write(peer1ipv6);

		byte[] fullDhtReplyMessage = prependSizeForMessage(
			outputStream.toByteArray()
		);
		
		out.write(fullDhtReplyMessage, 0, fullDhtReplyMessage.length);
	}

	/**
	 * Sends an Error reply.
	 * 
	 * @param messageName
	 * @throws Exception
	 */
	private static void sendErrorReply(String messageName) throws Exception {

		byte[] key = Arrays.copyOfRange(lastReceivedMessage, 4, 36);

		byte[] messageCode = Helper.networkOrderedBytesFromShort(
			(short) MessagesLegend.codeForName(messageName)
		);

		byte[] reserved = new byte[2];
		byte[] request_type = Helper.networkOrderedBytesFromShort((short) 10);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(messageCode);
		outputStream.write(request_type);
		outputStream.write(reserved);
		outputStream.write(key);

		byte[] fullDhtErrorMessage = prependSizeForMessage(
			outputStream.toByteArray()
		);
		
		out.write(fullDhtErrorMessage, 0, fullDhtErrorMessage.length);
	}
}
