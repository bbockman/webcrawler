package org.broox.space;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.broox.space.algo.BloomFilter;
import org.broox.space.algo.StockTickerCounter;
import org.broox.space.algo.Trie;
import org.broox.space.concur.TextDataHandler;
import org.broox.space.graph.WebCrawler;
import org.broox.space.inter.DatabaseWriter;
import org.broox.space.inter.NodeProcessor;
import org.broox.space.web.JedisIntWriter;
import org.broox.space.web.JsoupConnect;
import org.broox.space.web.JsoupProcessor;
import org.broox.space.web.URL;

import redis.clients.jedis.Jedis;


public class Main {
	
	public static void main(String[] args) throws IOException {
		URL base5 = new URL("https://www.bogleheads.org/forum/viewforum.php?f=2");
		URL base6 = new URL("https://www.biggerpockets.com/forums/519");
		URL base7 = new URL("https://www.reddit.com/r/personalfinance/");
		URL base8 = new URL("https://www.wisebread.com/9-online-forums-thatll-help-you-reach-your-financial-goals");
		URL base = new URL("https://www.reddit.com/r/wallstreetbets/");
		URL base2 = new URL("https://investorshub.advfn.com/boards/hubstocks.aspx");
		URL base3 = new URL("https://www.reddit.com/r/Daytrading/");
		URL base4 = new URL ("https://www.stockopedia.com/discussion/");
		
		List<URL> seed = List.of(base, base2, base4, base3, base4, base5, base6, base7, base8);
		List<String> tickers = TestRunner.tickers(TestRunner.json());
		BlockingQueue<String> queue = new ArrayBlockingQueue<>((int) 1E5);
		Jedis jedis = new Jedis("redis://localhost:6379");
		Trie trie = new Trie(tickers);
		JsoupConnect jsoup = new JsoupConnect();
		
		DatabaseWriter<String, Integer> dataWriter = new JedisIntWriter(jedis);
		StockTickerCounter stockticker = new StockTickerCounter(trie, dataWriter);
		
		TextDataHandler writer = new TextDataHandler(queue, stockticker);
		NodeProcessor<URL> processor = new JsoupProcessor(jsoup, writer);
		
		BloomFilter<URL> filter = new BloomFilter<>((int) 1E5);
		
		WebCrawler webCrawler = new WebCrawler(seed, (int) 2E3, (int) 10, processor, filter);	
		
		Thread crawl = new Thread(() -> webCrawler.run());
		Thread write = new Thread(() -> writer.run());
		crawl.start();
		write.start();
		
		System.out.println("Main Done.");
		
		
		
	}

}
