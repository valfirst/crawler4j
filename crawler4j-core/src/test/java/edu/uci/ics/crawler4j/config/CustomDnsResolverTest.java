package edu.uci.ics.crawler4j.config;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import crawlercommons.filters.basic.BasicURLNormalizer;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import edu.uci.ics.crawler4j.test.TestUtils;
import edu.uci.ics.crawler4j.url.SleepycatWebURLFactory;
import edu.uci.ics.crawler4j.url.WebURLFactory;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import java.io.File;
import java.net.InetAddress;

import org.apache.hc.client5.http.impl.InMemoryDnsResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

class CustomDnsResolverTest {

	@TempDir
  public File crawlStorageFolder;

    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
        .options(new WireMockConfiguration().dynamicPort())
        .build();


    @Test
    void visitJavascriptFiles() throws Exception {
        // given: "an index page"
        wm.stubFor(get(urlEqualTo("/some/index.html"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/html")
                .withBody(TestUtils.getInputStringFrom("/html/helloWorld.html")))
              );

        // when:
        final InMemoryDnsResolver inMemDnsResolver = new InMemoryDnsResolver();
        inMemDnsResolver.add("googhle.com"
                , InetAddress.getByName("127.0.0.1"));

        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(crawlStorageFolder.getAbsolutePath());
        config.setMaxPagesToFetch(10);
        config.setPolitenessDelay(1000);
        config.setDnsResolver(inMemDnsResolver);

        BasicURLNormalizer normalizer = TestUtils.newNormalizer();
        PageFetcher pageFetcher = new PageFetcher(config, normalizer);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        robotstxtConfig.setEnabled(false);
        WebURLFactory webURLFactory = new SleepycatWebURLFactory();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher, webURLFactory);
        CrawlController controller = new CrawlController(config, normalizer, pageFetcher, robotstxtServer, TestUtils.createFrontierConfiguration(config));

        controller.addSeed("http://googhle.com:" + wm.getPort() + "/some/index.html");
        controller.start(WebCrawler.class, 1);


        // then:
        wm.verify(exactly(1), getRequestedFor(urlEqualTo("/some/index.html")));
    }

}
