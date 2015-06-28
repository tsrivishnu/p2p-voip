package edu.tum.p2p.group20.voip.com;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

class Message {
	
	private static JSONParser jsonParser = new JSONParser();
	
	public JSONObject jsonObject;
	
	Message(String json) {
		
	}
}

public class MessageTest {

	public static void main(String[] args) throws ParseException {
		String json = "{\"content\": \"message\",\"something\": \"more\"}";
		JSONParser parser = new JSONParser();
		JSONObject message = (JSONObject) parser.parse(json);
		System.out.println(message.get("content"));
		
		
		String json2 = "{\"content\": \"message2\",\"something\": \"more2\"}";
		JSONObject message1 = (JSONObject) parser.parse(json2);
		System.out.println(message1.get("content"));
	}

}
