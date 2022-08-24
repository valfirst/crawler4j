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
