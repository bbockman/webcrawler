package org.broox.space;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

public class TestRunner {
	public static void main(String[] args) {
		
		List<String> tic = tickers(json());
		List<String> cnt = getValuesInBatches(tic, 100);
		List<List<Object>> ret = new ArrayList<>();
		for (int i = 0; i < tic.size(); ++i) {
			if (cnt.get(i) == null) continue;
			if (tic.get(i).length() < 4) continue;
			//System.out.println(tic.get(i) + ", " + cnt.get(i));
			ret.add(List.of(tic.get(i), cnt.get(i)));
		}
		
		Collections.sort(ret, (a,b)->Integer.compare(Integer.valueOf((String)b.get(1)),Integer.valueOf((String)a.get(1))));
		for (List<Object> o : ret) {
			System.out.println(o);
		}
		
	}
	
	public static List<String> getValuesInBatches(List<String> keys, int batchSize) {
		
		List<String> allValues = new ArrayList<>();
		for (int i = 0; i < keys.size(); i += batchSize) {
			int endIndex = Math.min(i + batchSize, keys.size());
			List<String> batch = keys.subList(i, endIndex);
			try (Jedis jedis = new Jedis("redis://localhost:6379")) {
				List<String> values = jedis.mget(batch.toArray(new String[0]));
				allValues.addAll(values);
			} catch (Exception ex) {
				System.out.println(ex);
			}
		}

		return allValues;
		

	}
	
	static void keys() {
		Jedis jedis = new Jedis("redis://localhost:6379");
		ScanParams scanParams = new ScanParams();
		scanParams.match("*");
		scanParams.count(100); // Adjust count based on system preference
		String cursor = "0";
		Set<String> keys = new HashSet<>();

		do {
		    ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
		    keys.addAll(scanResult.getResult());
		    // /cursor = scanResult.getStringCursor();
		} while (!cursor.equals("0")); 
		jedis.close();
	}
	
	static List<String> tickers(JSONObject jo) {
		List<String> ret = new ArrayList<>();
		for (Object o : jo.keySet()) {
			String kid = (String) ((JSONObject)jo.get(o)).get("ticker");
			ret.add(kid);
		}
		return ret;
	}
	
	static JSONObject json() {
		JSONParser parser = new JSONParser();
		try {
			Object obj = parser.parse(new FileReader("src/resources/Tickers.json"));
			JSONObject jsonObject = (JSONObject) obj;
	        String name = (String) jsonObject.get("Name");
	        System.out.println("Name: " + name);
	        return jsonObject;
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
        return null;
	}
	
	static Iterator<Entry<String, Integer>> test() {
		Map<String, Integer> map = new HashMap<>();
		map.put("AMZN", 2);
		map.put("AAPL", 4);
		map.put("MRSF", 5);
		map.put("GOOG", 1);
		
		Set<Entry<String, Integer>> set = map.entrySet();
		Iterator<Entry<String, Integer>> it = set.iterator();
		
		int attempts = 4;
		
		while (it.hasNext()) {
			Entry<String, Integer> next = it.next();
    		while (attempts > 0) {
    			try (Jedis jedis = new Jedis("redis://localhost:6379")) {
    				
    				
    				String old = jedis.get(next.getKey());
    				int oldVal = 0;
    				if (old != null) {
    					oldVal = Integer.valueOf(old);
    				}
    				int nextVal = next.getValue() + oldVal;
    				
    				String success = jedis.set(next.getKey(), String.valueOf(nextVal));
    				
    				System.out.printf("%s: %s: %d\n", success, next.getKey(), nextVal);
    				
    				if (success == null || !success.equals("OK")) {
    					throw new IOException("Failed to write new key value pair. " + 
    											"[" + next.getKey() + ", " + next.getValue() + "]");
    				}
    				break;
    			} catch (Exception ex) {
    				attempts--;
    				if (attempts == 0) {
    					return it;
    				}
    			}
    		}
		}
		
		return null;
	}
}
