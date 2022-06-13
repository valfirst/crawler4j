/*-
 * #%L
 * de.hs-heilbronn.mi:crawler4j-core
 * %%
 * Copyright (C) 2010 - 2022 crawler4j-fork (pre-fork: Yasser Ganjisaffar)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package edu.uci.ics.crawler4j.tests.parser;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.uci.ics.crawler4j.parser.CssParseData;
import edu.uci.ics.crawler4j.test.Crawler4jTestUtils;
import edu.uci.ics.crawler4j.test.TestUtils;
import edu.uci.ics.crawler4j.url.WebURL;

public class CssParseDataTest {
	
	/**
	 * <p>
	 * Remark: no urls should be found.
	 * </p><p>
	 * A problem was that the pattern inside CssParseData will match parts of data urls instead of the complete url.
	 * More specifically, following will get matched
	 * by the expression for "url(...)" instead of
	 * by the expression for "url("...")":
	 * url("...)...")
	 * <p>
	 */
	@Test
	void extractUrlInCssTextIgnoresDataUrlsFromBootstrapMinCssTest() {
		final String cssText = TestUtils.getInputStringFrom("/css/bootstrap.min.css");
		assertNoURLsFoundInCSS(cssText);
	}
	
	/**
	 * Remark: no urls should be found.
	 */
	@Test
	void extractUrlInCssTextIgnoresDataUrlFromBootstrapSubsetTest() {
		final String cssText = TestUtils.getInputStringFrom("/css/bootstrap.min-subset.css");
		assertNoURLsFoundInCSS(cssText);
	}
	
	/**
	 * REMARK: THE HOST OF AN ABSOLUTE URL SHOULD NOT BE ALTERED.
	 */
	@Test
	void extractAbsoluteUrlFromCssTest() {
		// This css is a subset of: https://fonts.googleapis.com/css?family=Lato|Sanchez:400italic,400|Abhaya+Libre
		final String cssText = TestUtils.getInputStringFrom("/css/fonts-absolute.css");
		assertDataUrlsFound(cssText//
		// This was previously the result, but is wrong
//				, "http://www.test.com/s/sanchez/v13/Ycm0sZJORluHnXbIfmxh_zQA.woff2"//
		// This is what should be the result
				, "https://fonts.gstatic.com/s/sanchez/v13/Ycm0sZJORluHnXbIfmxh_zQA.woff2"//
		);
	}
	
	@Test
	void extractRelativeUrlFromCssTest() {
		final String cssText = TestUtils.getInputStringFrom("/css/fonts-relative.css");
		assertDataUrlsFound(cssText//
				, "http://www.test.com/path/s/sanchez/v13/Ycm0sZJORluHnXbIfmxh_zQA.woff2"//
		);
	}
	
	private void assertNoURLsFoundInCSS(final String cssText) {
		final WebURL webURL = Crawler4jTestUtils.newWebURL("http://www.test.com/path/to/bootstrap.min.css");
		
		final CssParseData cssParseData = Crawler4jTestUtils.newCssParseData();
		cssParseData.setTextContent(cssText);
		cssParseData.setOutgoingUrls(webURL);
		final Set<WebURL> outgoingUrls = cssParseData.getOutgoingUrls();
		assertNotNull(outgoingUrls);
		Assertions.assertThat(outgoingUrls).isEmpty();
	}
	
	private void assertDataUrlsFound(final String cssText, final String... urls) {
		final WebURL webURL = Crawler4jTestUtils.newWebURL("http://www.test.com/path/to/bootstrap.min.css");
		
		final CssParseData cssParseData = Crawler4jTestUtils.newCssParseData();
		cssParseData.setTextContent(cssText);
		cssParseData.setOutgoingUrls(webURL);
		final Set<WebURL> outgoingUrls = cssParseData.getOutgoingUrls();
		
		Assertions.assertThat(outgoingUrls).hasSize(urls.length);
		Assertions.assertThat(outgoingUrls).map(t -> t.getURL()).isSubsetOf(urls);
	}
	
	
	@Test
	void cSSUrlsParsingQuotes() {
		CssParseData parseData = Crawler4jTestUtils.newCssParseData();
		parseData.setTextContent(TestUtils.getInputStringFrom("/css/quotes.css"));
		WebURL webUrl = Crawler4jTestUtils.newWebURL("http://example.com/css.css");
		
		parseData.setOutgoingUrls(webUrl);
		Set<WebURL> urls = parseData.getOutgoingUrls();
		
		Assertions.assertThat(urls).hasSize(3);
	}
	
	@Test
	void cSSAbsoluteUrlsPaths() {
		CssParseData parseData = Crawler4jTestUtils.newCssParseData();
		parseData.setTextContent(TestUtils.getInputStringFrom("/css/absolute.css"));
		// This use case is rather simple. A css file can contain absolute urls to resources that reside on a different host.
		WebURL webUrl = Crawler4jTestUtils.newWebURL("http://example.com/css.css");
		
		parseData.setOutgoingUrls(webUrl);
		Set<WebURL> urls = parseData.getOutgoingUrls();
		
		Assertions.assertThat(urls).hasSize(3);
		Assertions.assertThat(urls).map(t -> t.getURL()).isSubsetOf(//
				"http://example.com/css/absolute_no_proto.png"//
				, "http://example.com/css/absolute_path.png"//
				, "http://example.com/css/absolute_with_domain.png"//
		);
	}
	
	@Test
	void cSSRelativeUrlsPaths() {
		CssParseData parseData = Crawler4jTestUtils.newCssParseData();
		parseData.setTextContent(TestUtils.getInputStringFrom("/css/relative.css"));
		WebURL webUrl = Crawler4jTestUtils.newWebURL("http://example.com/asset/css/css.css");
		
		parseData.setOutgoingUrls(webUrl);
		Set<WebURL> urls = parseData.getOutgoingUrls();
		
		Assertions.assertThat(urls).hasSize(2);
		Assertions.assertThat(urls).map(t -> t.getURL()).isSubsetOf(//
				"http://example.com/asset/images/backgound_one.jpg"//
				, "http://example.com/backgound_two.jpg"//
		);
	}
}
