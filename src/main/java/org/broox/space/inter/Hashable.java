package org.broox.space.inter;

public interface Hashable extends Iterable<Long> {
	@Override public HashDataIterator iterator();
}

