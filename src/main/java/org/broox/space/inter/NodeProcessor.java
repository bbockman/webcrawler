package org.broox.space.inter;

import java.util.Collection;
import java.util.List;

public interface NodeProcessor<T> {
	
	public default void processNode(T node) {
		
	}
	
	public default Collection<T> findChildren(T node){
		return List.of();
	}
	
	public default boolean keepNode(T node) {
		return true;
	}
	
}
