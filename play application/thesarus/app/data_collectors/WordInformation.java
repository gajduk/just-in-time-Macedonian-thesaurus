package data_collectors;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;



public class WordInformation {
	
	List<String> definitions = new LinkedList<String>();              // definicii na makedonski - od leksikonite
	String word_type = "";                                            // tip na zbor, glagol, imenka
	List<String> translations = new LinkedList<String>();             // prevodi na zborot na angliski
	List<String> wikipedia_defs = new LinkedList<String>();;          //definicii na zborot od wikipedia 
	
	public WordInformation(String word) {
		getWordInformation(word);
	}
	
	public WordInformation getWordInformation(String word) {
		//check if the word is valid
		if (word == null || word.length() == 0) 
			return this;
		//Concurrently fetch the information from the web using an executor service
		ExecutorService executor = Executors.newFixedThreadPool(3);
		Future<MakedonskiInfo> minfo = executor.submit(new MakedonskiInfo(word));
		Future<List<String>> offnet = executor.submit(new OffNet(word));
		Future<List<String>> wikipedia = executor.submit(new Wikipedia(word));
		//wait 1000 ms max for the crawlers
		try {
			executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		executor.shutdown();
		//check each crawler if it has finished, if so collect the data
		if ( minfo.isDone() ) {
			try {
				this.definitions.addAll(minfo.get().definitions);
				this.word_type = minfo.get().word_type;
				this.translations.addAll(minfo.get().translations);
			} catch (Exception e) {
				e.printStackTrace();
			}
				
		}
		if ( offnet.isDone() )
			try { this.definitions.addAll(offnet.get());
			} catch (Exception e) {
				e.printStackTrace();
			}
		if ( wikipedia.isDone() ) 
			try { this.wikipedia_defs.addAll(wikipedia.get());
			} catch (Exception e) {
				e.printStackTrace();
			}
        
		//remove any duplicates
		this.translations = new ArrayList<String>(new HashSet<String>(this.translations));
		this.definitions = new ArrayList<String>(new HashSet<String>(this.definitions));
		this.wikipedia_defs = new ArrayList<String>(new HashSet<String>(this.wikipedia_defs));
		
		return this;
	}

}
