package utilities;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RegexUtils {
	
	public static String findFirst(String text,Pattern pattern) {
		Matcher m = pattern.matcher(text);
		if ( m.find() ) return m.group(1);
		return "";
	}
	
	public static List<String> findAll(String text,Pattern pattern) {
		List<String> res = new LinkedList<String>();
		Matcher m = pattern.matcher(text);
		while ( m.find() ) res.add(m.group(1));
		return res;
	}

}
