/*-
 * #%L
 * de.hs-heilbronn.mi:crawler4j-commons
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
package edu.uci.ics.crawler4j.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.test.SimpleWebURLFactory;
import edu.uci.ics.crawler4j.url.TLDList;
import edu.uci.ics.crawler4j.url.WebURL;

public class NetTest {
	
	/**
	 * edu.uci.ics.crawler4j.parser.Parser will force input "<html></html>" for binary content.
	 * No urls should be detected.
	 */
	@Test
	void extractUrlsTest() {
		final Net net = createNet(false);
		final Set<WebURL> result = net.extractUrls("<html></html>");
		assertNotNull(result);
		Assertions.assertThat(result).isEmpty();
	}
	
	/**
	 * <p>
	 * edu.uci.ics.crawler4j.parser.Parser will force input "<html></html>" for binary content.
	 * No urls should be detected.
	 * <p>
	 */
	@Test
	void extractUrlsWithAllowSingleLevelDomainTest() {
		final Net net = createNet(true);
		final Set<WebURL> result = net.extractUrls("<html></html>");
		assertNotNull(result);
		Assertions.assertThat(result).isEmpty();
	}
	
	private static Net createNet(final boolean allowSingleLevelDomain) {
		try {
			final CrawlConfig config = new CrawlConfig();
			config.setAllowSingleLevelDomain(allowSingleLevelDomain);
			final TLDList tldList = new TLDList(config);
			final SimpleWebURLFactory factory = new SimpleWebURLFactory();
			final Net net = new Net(config, tldList, factory);
			return net;
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
