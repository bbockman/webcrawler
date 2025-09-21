package org.broox.space.web;

import org.jsoup.Connection;
import org.jsoup.Jsoup;


public class JsoupConnect {
	public Connection connect(URL node) {
		return Jsoup.connect(node.toString());
	}
}
