package edu.uci.ics.crawler4j.crawler;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import crawlercommons.filters.basic.BasicURLNormalizer;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import edu.uci.ics.crawler4j.test.TestUtils;
import edu.uci.ics.crawler4j.url.SleepycatWebURLFactory;
import edu.uci.ics.crawler4j.url.WebURLFactory;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

class NoFollowTest {

    @TempDir
  public File crawlStorageFolder;

    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
        .options(new WireMockConfiguration().dynamicPort())
        .build();

    @Test
    void ignoreNofollowLinks() throws Exception {
        // given: "an index page with two links"
    	wm.stubFor(get(urlEqualTo("/some/index.html"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/html")
                        .withBody(TestUtils.getInputStringFrom("/html/noFollow/index.html")))
              );
    	wm.stubFor(get(urlPathMatching("/some/page(1|3).html"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/html")
                        .withBody(TestUtils.getInputStringFrom("/html/noFollow/page1_3.html")))
              );
    	wm.stubFor(get(urlPathMatching("/some/page2.html"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/html")
                        .withBody(TestUtils.getInputStringFrom("/html/noFollow/page2.html")))
              );

        // and: "an allow everything robots.txt"
    	wm.stubFor(get(urlPathMatching("/robots.txt"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/plain")
                        .withBody(TestUtils.getInputStringFrom("/robotstxt/robots.txt")))
              );

        // when:
        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(crawlStorageFolder.getAbsolutePath());
        config.setPolitenessDelay(100);
        config.setMaxConnectionsPerHost(1);
        config.setThreadShutdownDelaySeconds(1);
        config.setThreadMonitoringDelaySeconds(1);
        config.setCleanupDelaySeconds(1);

        BasicURLNormalizer normalizer = TestUtils.newNormalizer();
        WebURLFactory webURLFactory = new SleepycatWebURLFactory();
        PageFetcher pageFetcher = new PageFetcher(config, normalizer);
        RobotstxtServer robotstxtServer = new RobotstxtServer(new RobotstxtConfig(), pageFetcher, webURLFactory);
        CrawlController controller = new CrawlController(config, normalizer, pageFetcher, robotstxtServer, TestUtils.createFrontierConfiguration(config));
        controller.addSeed("http://localhost:" + wm.getPort() + "/some/index.html");

        controller.start(WebCrawler.class, 1);

        // then: "nofollow links should not be visited"
        wm.verify(exactly(1), getRequestedFor(urlEqualTo("/robots.txt")));
        wm.verify(exactly(0), getRequestedFor(urlEqualTo("/some/page1.html")));
        wm.verify(exactly(1), getRequestedFor(urlEqualTo("/some/page2.html")));
        wm.verify(exactly(0), getRequestedFor(urlEqualTo("/some/page3.html")));
    }
}
