package org.broox.space.web;

import org.broox.space.inter.HashDataIterator;
import org.broox.space.inter.Hashable;

public class URL implements Hashable {
	final String url;
	
	public URL(String url) {
		this.url = url;
	}
	
	public static URL of(String str) {
		return new URL(str);
	}
	
	public static URL of(int i) {
		return new URL(String.valueOf(i));
	}
	
	@Override
	public int hashCode() {
		return url.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof URL)) return false;
		URL other = (URL) obj;
		return this.url.equals(other.url);
	}
	
	public String toString() { 
		return url;
	}

	@Override
	public HashDataIterator iterator() {
		return new URLHasher();
	}
	
	private class URLHasher implements HashDataIterator {
		int idx = 0;
		
		@Override
		public boolean hasNext() {
			return idx < url.length();
		}
		
		@Override
		public Long next() {
			return Long.valueOf(url.charAt(idx++));
		}
	}
}

