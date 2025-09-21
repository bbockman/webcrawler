package org.broox.space.inter;

import java.util.Map;

public interface TableDataReader<K, V> {
	public Map<K, V> getData();
}
