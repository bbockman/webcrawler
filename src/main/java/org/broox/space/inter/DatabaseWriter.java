package org.broox.space.inter;

import java.util.Iterator;
import java.util.Map.Entry;

public interface DatabaseWriter<K, V> {
	public void write(Iterator<Entry<K, V>> obj);
}
