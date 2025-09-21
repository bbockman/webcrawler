package org.broox.space.algo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Trie {
	Map<Character, Trie> kids;
	String word;
	
	public Trie() {
		kids = new HashMap<>();
	}
	
	public Trie (List<String> seeds) {
		this();
		for (String s : seeds) {
			this.add(s);
		}
	}
	
	public void add(String str) {
		Trie current = this;
		for (char c : str.toCharArray()) {
			if (!current.kids.containsKey(c)) {
				current.kids.put(c, new Trie());
			}
			current = current.kids.get(c);
		}
		current.word = str;
	}
	
	public List<String> wordMatches(String input, int idx) {
		if (input == null) return List.of();
		
		Trie current = this;
		List<String> res = new ArrayList<>();
		
		while(current != null && idx < input.length()) {
			if (current.word != null) {
				if (idx + 1 >= input.length() || !isCharacter(input.charAt(idx + 1))) {
					res.add(current.word);
				}
			}
			current = current.kids.get(input.charAt(idx++));
		}
		if (current != null && current.word != null) 
			res.add(current.word);
		
		return res;
	}
	
	public String toString() {
		return word + "-->" + kids;
	}
	
	private static boolean isCharacter(char c) {
		return c != 'x' && (Character.isAlphabetic(c) || Character.isDigit(c));
	}
}
