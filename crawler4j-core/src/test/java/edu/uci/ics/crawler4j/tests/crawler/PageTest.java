/*
 * Copyright 2010-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.uci.ics.crawler4j.tests.crawler;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.BasicHttpEntity;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.test.Crawler4jTestUtils;
import edu.uci.ics.crawler4j.url.WebURL;

public class PageTest {
	
	@Disabled("Not possible to create UNPARSABLE ContentType + charset is ALWAYS set to StandardCharsets.UTF_8; (needs fix)")
	@Test
	void defaultCharsetFallback()
			throws IOException
	{
		String content = "The content";
		// "http entity with unsupported charset"
		HttpEntity entity = new BasicHttpEntity(//
				IOUtils.toInputStream(content, "UTF-8")//
				, content.length()//
				, ContentType.create("text/html", "UNPARSABLE")//
		);
		
		// "trying to load the entity"
		WebURL u = Crawler4jTestUtils.newWebURLFactory().newWebUrl();
		Page page = new Page(u);
		page.load(entity, 1024);
		
		// "charset should fallback to UTF-8"
		"UTF-8".equals(page.getContentCharset());
	}
}
