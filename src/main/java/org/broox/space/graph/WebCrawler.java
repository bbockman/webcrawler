package org.broox.space.graph;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Queue;

import org.broox.space.algo.BloomFilter;
import org.broox.space.inter.NodeProcessor;
import org.broox.space.web.URL;

public class WebCrawler implements Runnable {
	private NodeProcessor<URL> processor;
	private BloomFilter<URL> found;
	
	private final Queue<URL> que;
	private final int maxSearchBreadth;
	private final int maxSearchDepth;
	
	private int currentSearchDepth = 0;
	private int totalProcessed = 0;
	

	/**
	 * 
	 * @param seeds
	 * @param maxSearchBreadth
	 * @param maxSearchDepth
	 * @param stringParser
	 */
	public WebCrawler(Collection<URL> seeds, int maxSearchBreadth, int maxSearchDepth, NodeProcessor<URL> processor, BloomFilter<URL> found) {
		
		if (maxSearchBreadth > 1E8) {
			throw new IllegalArgumentException("Limit on search breadth is set to 100 million.");
		}
		if (1E8 / maxSearchBreadth < maxSearchDepth) {
			throw new IllegalArgumentException("Limit on total search volume is 100 million items.");
		}
		if (found.maxItems() < maxSearchBreadth * maxSearchDepth) {
			throw new IllegalArgumentException("Bloom filter table size should be large enough to support the total search volume.");
		}

		this.found = found;
		this.processor = processor;
		this.maxSearchBreadth = maxSearchBreadth;
		this.maxSearchDepth = maxSearchDepth;
		
		que = new ArrayDeque<>(seeds);
		for (URL seed : seeds) {
			found.add(seed);
		}
		System.out.printf("Bloom filter set for %,d entries.\n", found.size());
	}
	
	private void printStats() {
		System.out.printf("Starting search depth level: %,d.\n", currentSearchDepth);
		System.out.println("- ".repeat(15));
		System.out.printf("Items in queue: %,d.\n", que.size());
		System.out.printf("Unique items found: %,d.\n", found.getNumberOfEntries());
		System.out.printf("Current queue throughput: %,d.\n", totalProcessed);
		System.out.println("- ".repeat(15));
		System.out.printf("Current load factor: %,.2f.\n", found.getLoadFactor());
		System.out.printf("Ratio with target load: %,.2f.\n", found.getLoadTargetRatio());
		System.out.println("- ".repeat(15));
		System.out.printf("Next URL in queue: %s.\n", que.peek());
		System.out.println("- ".repeat(15));
		System.out.println("- ".repeat(15));
	}
	
	public void run() {
		
		
		boolean isFull = false;	
		while (!que.isEmpty() && currentSearchDepth < maxSearchDepth) {

			int size = que.size();
			printStats();

			for (int i = 0; i < size; ++i) {
    			URL current = que.poll();
   
    			processor.processNode(current);
    			
    			if (que.size() <= maxSearchBreadth / 2) {
    				isFull = false;
    			}
    			if (isFull) {
    				continue;
    			}
    			
    			
    			for (URL child : processor.findChildren(current)) {
    				
    				if (que.size() >= maxSearchBreadth) {
    					isFull = true;
    					break;
    				}
    				if (!processor.keepNode(child)) {
    					continue;
    				}
    				if (!found.contains(child)) {
    					found.add(child);
    					que.add(child);
    				}
    			}
			}
			
			totalProcessed += size;
			currentSearchDepth += 1;
		}
		
		processor.processNode(null);
		System.out.println("Crawl Done.");
	}

}


