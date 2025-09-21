package org.broox.space.algo;

import java.util.List;

import org.broox.space.inter.HashDataIterator;
import org.broox.space.inter.Hashable;

public class HashFunction {
	final long mod;
	final long pow;
	
	public HashFunction(long mod, long pow) {
		this.mod = mod;
		this.pow = pow;
	}
	
	public int hash(Hashable item) {
		HashDataIterator hasher = item.iterator();
		long ret = 0;
		while (hasher.hasNext()) {
			ret = ret * pow + hasher.next();
			ret = ret % mod;
		}
		return (int) ret;
	}
	
	public String toString() {
		return List.of(pow, mod).toString();
	}
}
