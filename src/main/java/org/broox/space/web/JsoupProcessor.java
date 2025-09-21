package org.broox.space.web;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.broox.space.concur.TextDataHandler;
import org.broox.space.inter.NodeProcessor;
import org.jsoup.nodes.Element;
import org.jsoup.parser.StreamParser;

public class JsoupProcessor implements NodeProcessor<URL> {
	JsoupConnect connector;
	TextDataHandler writer;
	
	public JsoupProcessor(JsoupConnect connector, TextDataHandler writer) {
		this.writer = writer;
		this.connector = connector;
	}
	
	@Override
	public void processNode(URL node) {
		if (node == null) {
			writer.takeData(null);
			return;
		}
		try (StreamParser streamer = connector.connect(node).execute().streamParser()) {
			Iterator<Element> it = streamer.iterator();
			while (it.hasNext()){
				Element el = it.next();
				String text = el.ownText();
				if (text != null) {
    				writer.takeData(text.trim());
				}
				el.remove();
			}
		} catch (IOException | IllegalArgumentException e) {}	
	}
	
	@Override
	public Collection<URL> findChildren(URL node) {
		Stream.Builder<String> urls = Stream.builder();
		
		
		try (StreamParser streamer = connector.connect(node).execute().streamParser()) {
			Element el;
			while ((el = streamer.selectNext("a")) != null) {
				urls.add(el.attr("abs:href"));
				el.remove();
			}
		} catch (IOException | IllegalArgumentException e) {
			// System.out.println(e);
		} 
		
		Stream<String> ret = urls.build();
		ret = ret.filter(str -> str.startsWith("http"));
		
		return ret.map(URL::new).collect(Collectors.toList());
	}
	
	@Override
	public boolean keepNode(URL url) {
		return true;
		/*
		 * try (StreamParser streamer =
		 * Jsoup.connect(url.toString()).execute().streamParser()){ return true; }catch
		 * (Exception ex) { return false; }
		 */
	}
}
