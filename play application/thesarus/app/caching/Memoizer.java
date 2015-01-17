package caching;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.googlecode.concurrentlinkedhashmap.Weighers;

public class Memoizer<A,V> implements Computable<A, V> {
	private final int max_size = 10_000;
	private final Computable<A, V> c;
	
	private final ConcurrentLinkedHashMap<A, Future<V>> cache
		= new ConcurrentLinkedHashMap.Builder().maximumWeightedCapacity(max_size).weigher(Weighers.entrySingleton()).build();
	
	
	public Memoizer(Computable<A, V> c) { this.c = c; }
	
	public V compute(final A arg) {
		//if the requested resource is not in the cache start an expensive internet lookup
		Future<V> f = cache.get(arg);
		if (f == null) {
			//wrap the internet lookup in a callable
			FutureTask<V> ft = new FutureTask<V>(new Callable<V>() {
				public V call() throws InterruptedException {
					return c.compute(arg);
				}
			});
			f = ft;
			//put the future result in the cache
			cache.put(arg, ft);
			//start the internet lookup
			ft.run();
		}
		try {
			//wait for the lookup to complete, then return the result
			return f.get();
		} catch (Exception e) {
			System.err.println("ERROR in memorizer");
			return null;
		}
	}
	
}