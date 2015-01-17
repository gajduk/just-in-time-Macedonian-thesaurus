package controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonController {
	
	private final static Gson gson = new Gson();

	public static String toJson (Object o) {
		return gson.toJson(o);
	}
}
