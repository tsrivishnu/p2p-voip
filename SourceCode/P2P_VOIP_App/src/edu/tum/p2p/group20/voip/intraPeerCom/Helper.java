package edu.tum.p2p.group20.voip.intraPeerCom;

public class Helper {

	public static byte[] networkOrderedBytesFromShort(short shortValue) {
		
		byte[] buff = new byte[2];
		
        // Big Endian
		buff[0] = (byte) (shortValue >> 8);
        buff[1] = (byte) shortValue;
        
        return buff;
	}
	
	public static short shortFromNetworkOrderedBytes(byte[] byteValues) {
		return (short)(((byteValues[0] & 0xFF) << 8) | (byteValues[1] & 0xFF));
	}
}
