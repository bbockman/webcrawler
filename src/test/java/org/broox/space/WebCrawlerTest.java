package org.broox.space;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.broox.space.algo.BloomFilter;
import org.broox.space.graph.WebCrawler;
import org.broox.space.inter.NodeProcessor;
import org.broox.space.web.URL;
import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class WebCrawlerTest {
	
	/**
	 * 	 * Adjacency list of graph for testing
	 * 
	 * 1 -> {2, 3, 4, 8}
	 * 2 -> {1, 2, 3, 4, 7}
	 * 3 -> {3, 4, 5, 9}
	 * 4 -> {10, 11}
	 * 5 -> {5, 9}
	 * 6 -> {5, 9}
	 * 7 -> {5, 10}
	 * 8 -> {8}
	 * 9 -> {7, 8}
	 * 10-> {}
	 * 11-> {}
	 * 
	 * expected traversal order with sufficiently sized queue
	 * 1 - 2 - 3 - 4 - 8 - 1 - 7 - 5 - 9 - 10 - 11 - null
	 * expected traversal order with a queue size of 3
	 * 1 - 2 - 3 - 4 - 5 - 9 - 10 - 7 - 8 - null
	 * expected traversal order when depth is limited to 2
	 * 1 - 2 - 3 - 4 - 8
	 * expected traversal order when skipping node 2
	 * 1 - 3 - 4 - 8 - 5 - 9 - 10 - 11 - 7
	 */
	
	final static List<URL> SEED = List.of(URL.of(1));
	
	final static List<List<URL>> GRAPH = List.of(List.of(),
												 List.of(URL.of(2), URL.of(3), URL.of(4), URL.of(8)),
			 									 List.of(URL.of(1), URL.of(2), URL.of(3), URL.of(4), URL.of(7)),
			 									 List.of(URL.of(3), URL.of(4), URL.of(5), URL.of(9)),
			 									 List.of(URL.of(10), URL.of(11)),
			 									 List.of(URL.of(5), URL.of(9)),
			 									 List.of(URL.of(5), URL.of(9)),
			 									 List.of(URL.of(5), URL.of(10)),
			 									 List.of(URL.of(8)),
			 									 List.of(URL.of(7), URL.of(8)),
			 									 List.of(),
			 									 List.of());
	
	@Mock
	BloomFilter<URL> mockFilter;
	
	@Mock
	NodeProcessor<URL> mockProcessor;
	
	WebCrawler underTest;

	

	/**
	 * Setup same graph form for each test, we will be checking traversal orders from different input parameters on a single graph.
	 * We will setup the visited set filter to behave as it should, contains:true only on first encounter.
	 * By default we will not filter out any nodes, we will filter out a specific node for testing later
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		// Set up return values for findChildren
		for (int i = 1; i <= 11; ++i) {
			URL url = URL.of(i);
			List<URL> children = GRAPH.get(i);
			when(mockProcessor.findChildren(url)).thenReturn(children);

			// Simulate first-time visit: false (not visited), then true (visited)
			when(mockFilter.contains(url)).thenReturn(false).thenReturn(true);
		}

		// General stubbing
		when(mockFilter.maxItems()).thenReturn(10000);
		when(mockProcessor.keepNode(any())).thenReturn(true);
	}
	
	
	@Test
	public void testNormalTraversal() {
		underTest = new WebCrawler(SEED, 100, 100, mockProcessor, mockFilter);
		underTest.run();
		
		InOrder inOrder = Mockito.inOrder(mockProcessor);
	    inOrder.verify(mockProcessor).processNode( argThat( equalsUrl(1) ) );
	    inOrder.verify(mockProcessor).processNode( argThat( equalsUrl(2)) );
	    inOrder.verify(mockProcessor).processNode( argThat( equalsUrl(3)) );
	    inOrder.verify(mockProcessor).processNode( argThat( equalsUrl(4)) );
	    inOrder.verify(mockProcessor).processNode( argThat( equalsUrl(8)) );
		inOrder.verify(mockProcessor).processNode( argThat( equalsUrl(1)) );
	    inOrder.verify(mockProcessor).processNode( argThat( equalsUrl(7)) );
	    inOrder.verify(mockProcessor).processNode( argThat( equalsUrl(5)) );
	    inOrder.verify(mockProcessor).processNode( argThat( equalsUrl(9)) );
	    inOrder.verify(mockProcessor).processNode( argThat( equalsUrl(10)) );
	    inOrder.verify(mockProcessor).processNode( argThat( equalsUrl(11)) );
		inOrder.verify(mockProcessor).processNode(isNull());
		verify(mockProcessor, times(12)).processNode(any());
	}
	
	@Test
	public void testSmallQueue() {
		underTest = new WebCrawler(SEED, 3, 100, mockProcessor, mockFilter);
		underTest.run();
		
		ArgumentCaptor<URL> captor = ArgumentCaptor.forClass(URL.class);
		verify(mockProcessor, times(10)).processNode(captor.capture());

		List<URL> actualCalls = captor.getAllValues();
		List<URL> expectedCalls = Arrays.asList(
    				URL.of(1), URL.of(2), URL.of(3), URL.of(4), URL.of(5), URL.of(9), URL.of(10), 
					URL.of(7), URL.of(8), null
			);

		assertEquals(expectedCalls, actualCalls);
	}
	
	@Test
	public void testSmallDepth() {
		underTest = new WebCrawler(SEED, 100, 2, mockProcessor, mockFilter);
		underTest.run();
		
		ArgumentCaptor<URL> captor = ArgumentCaptor.forClass(URL.class);
		verify(mockProcessor, times(6)).processNode(captor.capture());

		List<URL> actualCalls = captor.getAllValues();
		List<URL> expectedCalls = Arrays.asList(
    				URL.of(1), URL.of(2), URL.of(3), URL.of(4), URL.of(8), null
			);

		assertEquals(expectedCalls, actualCalls);
	}
	
	@Test
	public void testNodeSkip() {
		
		// we must reset mockProcessor in order to change the keepnNode output for node 2
		Mockito.reset((Object) mockProcessor);
		for (int i = 1; i <= 11; ++i) {
			when(mockProcessor.findChildren(URL.of(i))).thenReturn(GRAPH.get(i));
			if (i != 2) {
				when(mockProcessor.keepNode(URL.of(i))).thenReturn(true);
			}
		}
		when(mockProcessor.keepNode(URL.of(2))).thenReturn(false);
		//
		
		// Do normal traversal
		
		underTest = new WebCrawler(SEED, 100, 100, mockProcessor, mockFilter);
		underTest.run();
		
		// assert traversal order

		ArgumentCaptor<URL> captor = ArgumentCaptor.forClass(URL.class);
		verify(mockProcessor, times(10)).processNode(captor.capture());

		List<URL> actualCalls = captor.getAllValues();
		List<URL> expectedCalls = Arrays.asList(
    				URL.of(1), URL.of(3), URL.of(4), URL.of(8), URL.of(5), URL.of(9), URL.of(10), 
					URL.of(11), URL.of(7), null
			);

		assertEquals(expectedCalls, actualCalls);
	}
	
	@Test
	public void wheSearchBreadthTooLarge_assertNewThrowsIllegalArgumentException() {
		// maximum is 1E8
		ThrowingRunnable func = () -> new WebCrawler(SEED, 1 + (int)1E8, 100, mockProcessor, mockFilter);
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, func);
		assertEquals("Limit on search breadth is set to 100 million.", ex.getMessage());
	}
	
	@Test
	public void wheSearchVolumeTooLarge_assertNewThrowsIllegalArgumentException() {
		// Note search volume is breadth * depth and must be <= 1e8
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
				() -> new WebCrawler(SEED, (int) 5E7, 1 + (int) 5E7, mockProcessor, mockFilter));
		assertEquals("Limit on total search volume is 100 million items.", ex.getMessage());
	}
	
	@Test
	public void whenSuppliedFilterIsTooSmall_assertNewThrowsIllegalArgumentException() {
		Mockito.reset((Object) mockFilter);
		// note filter size should be at least maxBreadth * maxDepth = 100*100 = 10000
		when(mockFilter.maxItems()).thenReturn(9999);

		
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new WebCrawler(SEED, 100, 100, mockProcessor, mockFilter));
		assertEquals("Bloom filter table size should be large enough to support the total search volume.", ex.getMessage());
		
	}
	
	@Test
	public void whenSuppliedFilterJustBigEnough_assertNewDoesNotThrowAnyException() {
		Mockito.reset((Object) mockFilter);
		// note filter size should be at least maxBreadth * maxDepth = 100*100 = 10000
		when(mockFilter.maxItems()).thenReturn(10000);
		
		try  {
			underTest = new WebCrawler(SEED, 100, 100, mockProcessor, mockFilter);
		} catch (Exception ex) {
			fail("Exception thrown when none should: " + ex.getMessage());
		}
		
	}

	private ArgumentMatcher<URL> equalsUrl(int id) {
		return i -> i != null && i.equals(URL.of(id));
	}
}
