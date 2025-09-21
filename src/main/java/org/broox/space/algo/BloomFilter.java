package org.broox.space.algo;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.broox.space.inter.Hashable;
import org.broox.space.web.URL;

public class BloomFilter<T extends Hashable> {
	private static final Logger logger = Logger.getLogger(BloomFilter.class.getName());
	
	private final int k;
	private final int tableSize;
	private final int maximumItems;
	private final double targetLoadFactor;
	private final BitSet filter;
	private final Collection<HashFunction> funcs;
	
	private int currentItems = 0;
	private boolean log = true;
	
	/**
	 * Create a bloom filter for a default number of items, i.e. 1e5.
	 */
	public BloomFilter() {
		this(100000);
	}
	
	/**
	 * Let the load factor take a default value, i.e. 4*exp(1).  This yields 4 distinct hash functions for the set.
	 * @param maximumItems
	 */
	public BloomFilter (int maximumItems) {
		this(maximumItems, Math.exp(1) * 4);
	}
	
	/**
	 * 
	 * @param maximumItems Maximum number of unique items to be added to set. 
	 * @param targetLoadFactor Ratio of table size (n) to unique items (m) e.i. (n / m).  Factor should be greater than the 
	 * natural exponential exp(1) ~= 2.718.
	 */
	public BloomFilter (int maximumItems, double targetLoadFactor) {
		this.targetLoadFactor = targetLoadFactor;
		
		if (targetLoadFactor < Math.exp(1)) {
			throw new IllegalArgumentException("For load factor less than exp(1), use a standard BitSet instead.");
		}
		
		this.k = (int) (targetLoadFactor / Math.exp(1));
		
		this.maximumItems = maximumItems;
		
		
		if (Integer.MAX_VALUE / targetLoadFactor > maximumItems) {
			this.tableSize = (int) Math.ceil(maximumItems * targetLoadFactor);
		}else {
			this.tableSize = Integer.MAX_VALUE;
			System.out.println("Requested table size greater than MAX_INT. Load factor is not guaranteed.");
		}
		
		System.out.printf("Bloom filter collision probability: %.2f %%\n", Math.pow(k/targetLoadFactor, k)*100);
		
		filter = new BitSet(tableSize);
		
		funcs = generateHashFunctions(k);
		

	}
	
	public BloomFilter(BloomFilter<URL> other) {
		this.k = other.k;
		this.tableSize = other.tableSize;
		this.maximumItems = other.maximumItems;
		this.filter = other.filter;
		this.targetLoadFactor = other.targetLoadFactor;
		this.currentItems = other.currentItems;
		this.funcs = other.funcs;	
	}
	
	public void add(T item) {
		int count = 0;
		for (HashFunction h : funcs) {
			int hash = h.hash(item) % tableSize;
			if (!filter.get(hash)) {
				++count;
				filter.set(hash);
			}
		}
		if (count > 0) {
			++currentItems;
		}
		if (log && currentItems > maximumItems) {
			String str = "Target load factor exceeded, probabily of collisions no longer guaranteed.";
			logger.warning(str);
			System.out.println(str);
			log = false;
		}
	}
	
	public boolean contains(T item) {
		int count = k;
		for(HashFunction h : funcs) {
			int hash = h.hash(item) % tableSize;
			if (filter.get(hash)) --count;
		}
		
		return count == 0;
	}
	
	public String toString() {
		return String.format("Hash Functions : %s \n"
						   + "Hash Table : %s \n", funcs, filter);
	}
	
	public int size() {
		return filter.size();
	}
	
	public int maxItems() {
		return maximumItems;
	}
	
	public int getNumberOfEntries() {
		return currentItems;
	}
	
	public double getLoadFactor() {
		double ret = (filter.size() / ((double) currentItems));
		return ret > filter.size() ? Double.POSITIVE_INFINITY : ret;
	}
	
	public double getLoadTargetRatio() {
		double ret = (maximumItems / ((double) currentItems));
		return ret > maximumItems ? Double.POSITIVE_INFINITY: ret;
	}
	
	private static Collection<HashFunction> generateHashFunctions(int count) {
		
		List<HashFunction> list = new ArrayList<>();
		Sieve primes = Sieve.forHasher();
		
		while (count-- > 0) {
			list.add(new HashFunction(primes.getMod(), primes.getPow()));
		}
		
		return list;
		
	}
}


