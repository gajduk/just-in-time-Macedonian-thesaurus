package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import caching.Computable;
import caching.Memoizer;
import data_collectors.WordInformation;

public class ServiceController extends Controller {
	private final static Computable<String, WordInformation> c =
			new Computable<String, WordInformation>() {
				public WordInformation compute(String arg) {
					return new WordInformation(arg);
				}
			};
	private final static Computable<String,WordInformation> cache
			= new Memoizer<String,WordInformation>(c);


    public static void index(String word) {
    	if ( word == null || word.length() == 0 )
    		renderJSON("");
    	WordInformation wi = null;
    	try {
    		wi = cache.compute(word);
		} catch (InterruptedException e) {
			System.err.println("Error when searching in cache");
    		renderJSON("");
		}
    	renderJSON(JsonController.toJson(wi));
    }

}