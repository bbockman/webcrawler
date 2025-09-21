package org.broox.space.algo;

import java.util.Arrays;
import java.lang.Math;

public class Sieve {
	private boolean[] sieve;
	private final static int MAX_MOD = (int) Math.sqrt(Integer.MAX_VALUE) ;// Math.pow(Integer.MAX_VALUE, 2.0/3.0); // .sqrt(Integer.MAX_VALUE);
	private final static int MIN_MOD = MAX_MOD / 2;
	private final static int MAX_POW = (int) Math.sqrt(MIN_MOD);
	private final static int MIN_POW = 29;
	private int current = MIN_POW;
	
	public Sieve(int size) {
		if (size <= 0 || size == Integer.MAX_VALUE) throw new IllegalArgumentException("Size must be > 0 and < Integer.MAX_VALUE."); 
		initialize(size);
	}
	
	public static Sieve forHasher() {
		return new Sieve(MAX_POW);
	}
	
	public Long getMod() {
		return Long.valueOf(Integer.MAX_VALUE);
	}
	
	public Long getPow() {
		current = getPrime(current);
		return Long.valueOf(current++);
	}
	
	public Integer getPrime(int min, int max) {
		if (min <= 0) throw new IllegalArgumentException("Sieve is only for non-negative prime numbers.");
		if (max < min) throw new IllegalArgumentException("Max must be greater than min.");
		if (max >= sieve.length)throw new IllegalArgumentException("Current Sieve was created with a lower max than requested in getPrime(min, max)."); 
		
		int attempts = 10;
		while (attempts > 0) {
			int  rnd = ((int) (Math.random() * (max-min+1))) + min;
			Integer out = getPrime(rnd);
			if (out <= max) return out;
			--attempts;
		}
		
		throw new IllegalStateException("Failed to find prime in given range after 10 attempts.");
	}
	
	public Integer getPrime(int min) {
		if (min <= 0) throw new IllegalArgumentException("Sieve is only for non-negative prime numbers.");
		if (min >= sieve.length)throw new IllegalArgumentException("Current Sieve was created with a lower max than requested in getPrime(min)."); 
		while (min < sieve.length && !sieve[min]) {
			min++;
		}
		
		if (min < sieve.length) {
			return min;
		}else{
			throw new IllegalArgumentException("No primes found in specified range.  "
					+ "You may want a larger Sieve, note Integer.MAX_VALUE is prime, but this class does not support that value.");
		}
	}
	
	private void initialize(int size) {
		int last = (int) Math.sqrt(size);
		sieve = new boolean[size+1];
		Arrays.fill(sieve, true);
		sieve[0] = false; sieve[1] = false;
		
		for (int num = 2; num <= last; num++) {
			if (!sieve[num]) continue;
			for (int mult = 2*num; mult <= size; mult += num) {
				sieve[mult] = false;
			}
		}
	}

}
