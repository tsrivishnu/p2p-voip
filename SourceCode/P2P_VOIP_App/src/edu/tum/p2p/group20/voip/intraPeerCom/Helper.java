package edu.tum.p2p.group20.voip.intraPeerCom;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;

public class Helper {

	/**
	 * Returns the byte array from a short value with BigEndian order which 
	 * is the standard network byte order
	 * 
	 * @param shortValue
	 * @return byte[] corresponding to the value in network byte order
	 */
	public static byte[] networkOrderedBytesFromShort(short shortValue) {
		
		byte[] buff = new byte[2];
		
        // Big Endian
		buff[0] = (byte) (shortValue >> 8);
        buff[1] = (byte) shortValue;
        
        return buff;
	}
	
	/**
	 * Returns the short values calculated from network ordered byte array values
	 * 
	 * @param byte[] byteValues
	 * @return short value of the corresponding byte array in network byte order
	 */
	public static short shortFromNetworkOrderedBytes(byte[] byteValues) {
		return (short)(((byteValues[0] & 0xFF) << 8) | (byteValues[1] & 0xFF));
	}
	
	/**
	 * Re-encapsulates the host info containing peerid, kxPort, reserved and ip details
	 * from DHT trace reply to kxport, reserved, peerid and ip details and required to
	 * send in  kx tunnel build.
	 * 
	 * @param infoFromDht
	 * @return
	 * @throws IOException
	 */
	public static byte[] trasnformXChangePointInfoFromDhtToKx(byte[] infoFromDht) throws IOException {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		byteStream.write(Arrays.copyOfRange(infoFromDht, 32, 36)); //kxport and reserved
		byteStream.write(Arrays.copyOfRange(infoFromDht, 0, 32)); // peerid
		byteStream.write(Arrays.copyOfRange(infoFromDht, 36, infoFromDht.length));
		
		return byteStream.toByteArray();
	}
}
