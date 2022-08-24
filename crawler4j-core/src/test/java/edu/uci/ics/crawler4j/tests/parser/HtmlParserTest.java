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

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.FileEntity;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.parser.HtmlParser;
import edu.uci.ics.crawler4j.parser.TikaHtmlParser;
import edu.uci.ics.crawler4j.test.Crawler4jTestUtils;
import edu.uci.ics.crawler4j.url.WebURL;
import edu.uci.ics.crawler4j.url.WebURLFactory;

public class HtmlParserTest {
	
	@Test
	void canParseHtmlPageTest() {
		WebURLFactory webURLFactory = Crawler4jTestUtils.newWebURLFactory();
		HtmlParser parser = new TikaHtmlParser(//
				new CrawlConfig(), Crawler4jTestUtils.newNormalizer(), Crawler4jTestUtils.newTLDList(), webURLFactory);
		WebURL url = webURLFactory.newWebUrl();
		url.setURL("http://wiki.c2.com/");
		File file = new File("src/test/resources/html/wiki.c2.com.html");
		ContentType contentType = ContentType.create("text/html", StandardCharsets.UTF_8);
		FileEntity entity = new FileEntity(file, contentType);
		Page page = new Page(url);
		
		Assertions.assertThatNoException().isThrownBy(() -> {
			page.load(entity, 1_000_000);
			parser.parse(page);
		});
	}
}
