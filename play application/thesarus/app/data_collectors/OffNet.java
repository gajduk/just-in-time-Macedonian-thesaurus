package data_collectors;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import utilities.RegexUtils;
import utilities.UrlFetcher;


public class OffNet implements Callable<List<String>> {
	
	private static final String  base_url = "http://recnik.off.net.mk/recnik/makedonski-leksikon/";
	private static final Pattern description_pattern = Pattern.compile("<div class=\"opis\">(.*?)</div>",Pattern.DOTALL);
	private String word;
	
	public OffNet(String word) {
    	this.word = word;
    }
	
	public String getFullUrl(String word) {
		return base_url+word;
	}
	
	public List<String> getWordDefiniions(String word) {
        if ( word.trim().indexOf(' ') > -1 ) return Collections.emptyList();
		String html = "";
		try {
			html = UrlFetcher.fetchGet(getFullUrl(word));
		} catch (IOException e) {
			System.out.println("Cant find the word on OffNet");
			return Collections.emptyList();
		}
		List<String> res = new LinkedList<String>();
		String definitions = RegexUtils.findFirst(html, description_pattern);
		String s_definitions[] = definitions.split("[0-9]\\.");
		for ( int i = 1 ; i < s_definitions.length ; ++i )
			res.add(s_definitions[i]);
		return res;
	}
	
	@Override
    public List<String>  call() throws Exception {
		return getWordDefiniions(word);         
    }

}
