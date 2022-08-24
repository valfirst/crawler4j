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
