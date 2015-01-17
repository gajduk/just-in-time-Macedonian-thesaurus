package data_collectors;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import utilities.RegexUtils;
import utilities.UrlFetcher;


public class MakedonskiInfo  implements Callable<MakedonskiInfo> {
	
		private static final String  base_url = "http://www.makedonski.info/";
		private static final Pattern description_pattern = Pattern.compile("<div class=\"meaning\" >(.*?)</div>",Pattern.DOTALL);
		private static final Pattern word_type_pattern = Pattern.compile("<div class=\"grammar\" >.*?<i>(.*?)</i>",Pattern.DOTALL);
        private static final Pattern translation_pattern = Pattern.compile("<a href=\"/ontology(.*?)</a>");
        // "<i>Англиски:</i>.*?>(.*?)</a>"
        private String word;
        
        public MakedonskiInfo(String word) {
        	this.word = word;
        }
	              		
		public String getFullUrl(String word) {
			return base_url+"letter/"+word.substring(0, 1)+"/"+word+"/"+word;
		}

		public MakedonskiInfo processWord(String word) {
            if ( word.trim().indexOf(' ') > -1 ) return this;
			String html = "";
			try {
				html = UrlFetcher.fetchGet(getFullUrl(word));
			} catch (IOException e) {
				System.out.println("Cant find the word on MakedonskiInfo");
				return this;
			}
			this.definitions = (List<String>) new LinkedList<String>();
			
			List<String> definitions_temp = new LinkedList<String>(RegexUtils.findAll(html, description_pattern));
	        for ( String s : definitions_temp ) {
	        	this.definitions.add(s.trim().replaceAll("<span >[0-9]\\.</span>", "").replaceAll("\n", "").trim());
	        }
			
			String word_type = RegexUtils.findFirst(html, word_type_pattern);
			if ( word_type.length() > 1 )
				this.word_type = word_type;
			
			//this.translations = RegexUtils.findAll(html, translation_pattern);
			this.translations = (List<String>) new LinkedList<String>();
			List<String> translations_temp = new LinkedList<String>(RegexUtils.findAll(html, translation_pattern));
			for (String s : translations_temp) {
				translations.add(s.replaceAll("(.*?)>", ""));
			}
			
			return this;
		}
		
		List<String> definitions = Collections.emptyList();		
		String word_type = "";		
		List<String> translations = Collections.emptyList();
		
		@Override
	    public MakedonskiInfo call() throws Exception {
			processWord(word);
	        return this; 
	    }
		
}
