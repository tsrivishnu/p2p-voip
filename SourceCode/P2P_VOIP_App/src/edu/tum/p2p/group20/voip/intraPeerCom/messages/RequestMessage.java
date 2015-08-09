package edu.tum.p2p.group20.voip.intraPeerCom.messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import edu.tum.p2p.group20.voip.intraPeerCom.Helper;
import edu.tum.p2p.group20.voip.intraPeerCom.MessagesLegend;

/**
 * Class that serves as parent class and handles basic functionalities of 
 * encapsulating required fields in a intra peer communication's request messages into
 * the required format
 * 
 * @author Sri Vishnu Totakura <srivishnu@totakura.in>
 *
 */
public class RequestMessage {
	/**
	 * Fields that are part of the message.
	 * Override these for each message child class
	 */
	public static String fields[] = {
      "size",
      "messageCode"
	};
	
	/**
	 * Message name which determines the message code.
	 * Override this for each message child class
	 */
	public static String messageName = ""; 
	
	public Map<String, byte[]> byteValues = new HashMap<String, byte[]>();
	
	/**
	 * Initializes an object
	 * Sets the size value to empty byte array of size 2 and adds the bytes
	 * for the message code
	 */
	public RequestMessage() {
		byteValues.put("size", new byte[2]); // put empty bytes for size for now

		byteValues.put(
			"messageCode",
			Helper.networkOrderedBytesFromShort(
				(short) MessagesLegend.codeForName(messageName)
			)
		);
	}
	
	/**
	 * Returns the full encapsulated message as byte array that can be 
	 * sent over the network
	 * 
	 * @return full encapsulated message as byte[]
	 * @throws IOException
	 */
	public byte[] fullMessageAsBytes() throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		calculateSizeAndAssign(); // Ensure size is calculated.
		
		for(int i=0; i < fields.length; i++) {
			outputStream.write(byteValues.get(fields[i]));
		}
		
		return outputStream.toByteArray();
	}
	
	private void calculateSizeAndAssign() {
		byteValues.put("size", sizeAsBytes());
	}
	
	/**
	 * Returns the byte[] for the integer value of total bytes in the message
	 * Note: return byte array is in network byte order.
	 * 
	 * @return total number of bytes in the message represented as byte[]
	 */
	private byte[] sizeAsBytes() {
		return Helper.networkOrderedBytesFromShort(totalBytesAsShort());
	}
		
	/**
	 * 
	 * 
	 * @return the total number of bytes in the full message
	 */
	private short totalBytesAsShort() {
		short totalBytes = 0;
		
		for(int i=0; i < fields.length; i++) {
			int fieldLength = byteValues.get(fields[i]).length;
			totalBytes = (short) (totalBytes + fieldLength);			
		}
		
		return totalBytes;
	}
	
	public static String messageCodeFromFullMessage(byte[] messageBytes) {
		short messageCode = Helper.shortFromNetworkOrderedBytes(
			Arrays.copyOfRange(messageBytes, 2, 4)
		);
		
        return MessagesLegend.nameForCode(messageCode);
	}
}
