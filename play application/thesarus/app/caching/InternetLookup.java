package caching;

import data_collectors.WordInformation;

public class InternetLookup implements Computable<String, WordInformation> { 
	
	@Override
	public WordInformation compute(String arg) throws InterruptedException {
		return new WordInformation(arg);
	}

}


