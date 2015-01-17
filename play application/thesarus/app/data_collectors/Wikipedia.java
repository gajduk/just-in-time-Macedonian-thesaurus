package data_collectors;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import utilities.RegexUtils;
import utilities.UrlFetcher;


public class Wikipedia implements Callable<List<String>> {
	
	private static final String  base_url = "http://mk.wikipedia.org/wiki/";
	private static final Pattern description_pattern = Pattern.compile("<p>(.*?)</p>",Pattern.DOTALL);
	private static final Pattern link_pattern = Pattern.compile("<li>.*?<a href=\"/wiki/(.*?)\" title=.*?</li>",Pattern.DOTALL);
	private static final Pattern empty_page_1 = Pattern.compile("Википедија засега нема статија со овој наслов!");
	private static final Pattern empty_page_2 = Pattern.compile("Таква страница сè уште не постои. Можете да проверите");
	private static final Pattern empty_page_3 = Pattern.compile("Википедија нема страница со токму овој наслов!");
	private static final Pattern disambiguation_pattern = Pattern.compile("води овде, можеби би сакале да ја промените да води непосредно до соодветната статија.");
	private static final Pattern infobox_pattern = Pattern.compile("<table.*?class=\"infobox\".*?</table>",Pattern.DOTALL);
	private String word;
	
	public Wikipedia(String word) {
		this.word = word;	
	}

	public String getFullUrl(String word) {
		return base_url+word.replaceAll(" ","%20");
	}
	
	public List<String> getWordDefinitions(String word) {
		String html = "";
		try {
			html = UrlFetcher.fetchGet(getFullUrl(word));
		} catch (IOException e) {
			System.out.println("Cant find the word on Wikipedia");
			return Collections.emptyList();
		}
		if ( empty_page_1.matcher(html).find() ||
				empty_page_2.matcher(html).find() ||
				empty_page_3.matcher(html).find()  ) 
			return Collections.emptyList();
		//int i = 1;
		if ( disambiguation_pattern.matcher(html).find() ) {
			List<String> res = new LinkedList<String>();
			List<String> links = RegexUtils.findAll(html, link_pattern);
			int max_links_to_follow =  Math.min(5,links.size()-1);
			ExecutorService executor = Executors.newFixedThreadPool(max_links_to_follow);
			//this must be paralelized - invoke as many requests as possible
			LinkedList<Future<String>> futures = new LinkedList<>();
			final String query_word = word;
			for ( final String link : links.subList(0,max_links_to_follow) ) {
				if ( link.contains("amp;redlink=1")) continue;
				futures.add(executor.submit(new Callable<String>() {
					@Override
					public String call() throws Exception {
						String html = UrlFetcher.fetchGet(base_url+link);
						String description = getDescriptionForHtml(html, query_word);
						return description;
					}
				}));
			}
			try {
				executor.awaitTermination(900,TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			executor.shutdown();
			for ( Future<String> each : futures ) {
				if ( each.isDone() ) {
					try {
						if ( each.get().length() > 0 )
							res.add(each.get());
					} catch (InterruptedException | ExecutionException e) {
						e.printStackTrace();
					}
			}
			}
			return res;
		}
		else {
			String descr = getDescriptionForHtml(html, word);
			return Collections.singletonList(descr);
		}			
	}
	
    public String getDescriptionForHtml(String html, String word) {
        html = infobox_pattern.matcher(html).replaceAll("");
		String descr = RegexUtils.findFirst(html, description_pattern);		
        descr = descr.replaceAll("<(.*?)>", "").replaceAll("\\n","");
        if ( descr.length() < 150 ) return descr;
		return getFirstSentence(descr);
	}
    
    public String getFirstSentence(String descr) {
        String res = descr.split("\\. [АБВГДЃЕЖЗЅИЈКЛЉМНЊОПРСТЌУФХЦЧЏШ]")[0];
        if ( res.endsWith(".") ) return res;
        else return res+".";
    }

	@Override
	public List<String> call() throws Exception {
		return getWordDefinitions(word);
	}
	
}
