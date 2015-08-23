package edu.tum.p2p.group20.voip.intraPeerCom;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import edu.tum.p2p.group20.voip.intraPeerCom.messages.ReceivedMessage;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.ReceivedMessageFactory;
import edu.tum.p2p.group20.voip.intraPeerCom.messages.RequestMessage;

/**
 * Class responsible for sending and receiving messages among Voip module and KX
 * and DHT
 * 
 * @author Sri Vishnu Totakura <srivishnu@totakura.in>, Anshul Vij
 *
 */
public class IntraPeerCommunicator {

	public Socket clientSocket;
	public OutputStream out;
	public InputStream in;

	/**
	 * Initializes a communicator object to connect to a given ip and port
	 * 
	 * @param ip
	 * @param portNumber
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public IntraPeerCommunicator(String ip, int portNumber)
			throws UnknownHostException, IOException {

		clientSocket = new Socket(ip, portNumber);
		clientSocket.setSoTimeout(10000); // 10 Seconds timeout
		out = clientSocket.getOutputStream();
		in = clientSocket.getInputStream();
	}

	public void run() throws Exception {

		// Override this!
	}

	/**
	 * Sends the request message to the required endpoint as specified during
	 * initialize
	 * 
	 * @param requestMessage
	 * @throws IOException
	 */
	public void sendMessage(RequestMessage requestMessage) throws IOException {

		byte[] messageBytes = requestMessage.fullMessageAsBytes();
		System.out.println("Sending message: " + requestMessage.messageName());
		out.write(messageBytes, 0, messageBytes.length);
	}

	/**
	 * Receives a new message and returns an instance of ReceivedMessage
	 * 
	 * @return ReceivedMessage
	 * @throws Exception
	 */
	public ReceivedMessage readIncomingAndHandleError() throws Exception {

		ReceivedMessage receivedMessage = readIncomingMessage();
		raiseExceptionIfError(receivedMessage);
		return receivedMessage;
	}

	/**
	 * Raises an exception of last message received was of an ErrorType like
	 * DHT_ERROR or KX_ERROR
	 * 
	 * @param lastReceivedMessage
	 * @throws Exception
	 */
	private void raiseExceptionIfError(ReceivedMessage lastReceivedMessage)
			throws Exception {

		if (lastReceivedMessage != null && lastReceivedMessage.isErrorType()) {
			throw new Exception("Error message Received: "
					+ lastReceivedMessage.name());
		}
	}

	/**
	 * Receives an incoming message and returns it. This method encapsulates the
	 * functionality on how the message is received and creates an object of
	 * ReceivedMessage from the received bytes and returns it.
	 * 
	 * @return ReceivedMessage
	 * @throws Exception
	 */
	private ReceivedMessage readIncomingMessage() throws Exception {

		try {
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

			ReceivedMessage lastReceivedMessage = ReceivedMessageFactory
					.getReceivedMessageFor(byteStream.toByteArray());

			System.out.println("Received message: "
					+ lastReceivedMessage.name());

			return lastReceivedMessage;

		} catch (IOException e) {
			System.out
					.println("Exception caught while trying to read network message");
			System.out.println(e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Validates the message based on the expected name and pseudoId that is
	 * expected to be contained in the message
	 * 
	 * @param receivedMessage
	 * @param expectedName
	 * @param pseudoId
	 * @return boolean
	 * @throws Exception
	 */
	protected boolean isValidMessage(ReceivedMessage receivedMessage,
			String expectedName, byte[] pseudoId) throws Exception {

		return (receivedMessage != null
				&& receivedMessage.name().equals(expectedName) && receivedMessage
					.isValid(pseudoId));
	}
}
