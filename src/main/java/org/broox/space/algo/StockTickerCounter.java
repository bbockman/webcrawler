package org.broox.space.algo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.broox.space.inter.DatabaseWriter;
import org.broox.space.inter.TextProcessor;

public class StockTickerCounter implements TextProcessor {
	DatabaseWriter<String, Integer> dataWriter;
	public Trie trie;
	
	public StockTickerCounter(Trie trie, DatabaseWriter<String, Integer> dataWriter) {
		this.trie = trie;
		this.dataWriter = dataWriter;
	}
	
	@Override
	public void processText(String textToWrite) {
		Map<String, Integer> data = new HashMap<>();
		int n = textToWrite.length();
		for (int i = 0; i < n; i++) {
			if (i > 0 && isCharacter(textToWrite.charAt(i-1))) continue;
			List<String> matches = trie.wordMatches(textToWrite, i);
			for (String str : matches) {
				data.put(str, data.getOrDefault(str, 0) + 1);
			}
		}
		
		dataWriter.write(data.entrySet().iterator());
	}
	
	public Trie getTrie() {
		return trie;
	}
	
	private static boolean isCharacter(char c) {
		return c != 'x' && (Character.isDigit(c) || Character.isAlphabetic(c));
	}
	
}
