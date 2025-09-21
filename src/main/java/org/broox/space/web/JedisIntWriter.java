package org.broox.space.web;

import java.util.Iterator;
import java.util.Map.Entry;

import org.broox.space.inter.DatabaseWriter;

import redis.clients.jedis.Jedis;

public class JedisIntWriter implements DatabaseWriter<String, Integer>{
	private Jedis jedis;
	
	public JedisIntWriter(Jedis jedis) {
		this.jedis = jedis;
	}
	
	public void write(Iterator<Entry<String, Integer>> it) {
        while (it.hasNext()) {
        	
			Entry<String, Integer> next = it.next();
			
			for (int attempts = 0; attempts < 4; attempts++) {
				String old = jedis.get(next.getKey());
				
				int nextVal = next.getValue() + (old == null ? 0 : Integer.valueOf(old));
				
				String success = jedis.set(next.getKey(), String.valueOf(nextVal));
				if (success.equals("OK")) break;
			}
        }
        
	}
}
