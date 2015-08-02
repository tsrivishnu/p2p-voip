package edu.tum.p2p.group20.voip.intraPeerCom.messages.dht;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import edu.tum.p2p.group20.voip.intraPeerCom.Helper;
import edu.tum.p2p.group20.voip.intraPeerCom.MessagesLegend;

public class IntraPeerMessage {
	public static String fields[] = {
      "size",
      "messageCode"
	};
	
	public static String messageName = ""; 
	
	public Map<String, byte[]> byteValues = new HashMap<String, byte[]>();
	
	public IntraPeerMessage() {
		byteValues.put("size", new byte[2]); // put empty bytes for size for now
		System.out.println(Arrays.toString(fields));
		byteValues.put(
			"messageCode",
			Helper.networkOrderedBytesFromShort(
				(short) MessagesLegend.codeForName(messageName)
			)
		);
	}
	
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
	
	private byte[] sizeAsBytes() {
		return Helper.networkOrderedBytesFromShort(totalBytesAsShort());
	}
		
	private short totalBytesAsShort() {
		short totalBytes = 0;
		
		for(int i=0; i < fields.length; i++) {
			int fieldLength = byteValues.get(fields[i]).length;
			totalBytes = (short) (totalBytes + fieldLength);			
		}
		
		return totalBytes;
	}
}
