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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

class TimeoutTest {

    @TempDir
  public File crawlStorageFolder;

    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
        .options(new WireMockConfiguration().dynamicPort())
        .build();

    @Test
    void interceptSocketTimeoutException() throws Exception {
        // given: "an index page with two links will fail to respond in time"
    	wm.stubFor(get(urlEqualTo("/some/index.html"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/html")
                .withBody(TestUtils.getInputStringFrom("/html/timeout/index.html"))
                .withFixedDelay(60 * 1_000)
        ));

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
      config.setConnectionTimeout(10 * 1_000);

        BasicURLNormalizer normalizer = TestUtils.newNormalizer();
        WebURLFactory webURLFactory = new SleepycatWebURLFactory();
        PageFetcher pageFetcher = new PageFetcher(config, normalizer);
        RobotstxtServer robotstxtServer = new RobotstxtServer(new RobotstxtConfig(), pageFetcher, webURLFactory);
        CrawlController controller = new CrawlController(config, normalizer, pageFetcher, robotstxtServer, TestUtils.createFrontierConfiguration(config));
        controller.addSeed("http://localhost:" + wm.getPort() + "/some/index.html");

        controller.start(VisitAllCrawler.class, 1);


        // then:
        Page p = (Page) controller.getCrawlersLocalData().get(0);
    		Assertions.assertThat(p.getStatusCode()).isEqualTo(0);
				Assertions.assertThat(p.getFetchResponseHeaders()).hasSize(0);
    }

    @Test
    void responseCodeAndHeaderArePresentWhenReadTimeOut() throws Exception {
        // given: "an index page with two links very slow to respond"
    		wm.stubFor(get(urlEqualTo("/some/index.html"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/html")
                .withBody(TestUtils.getInputStringFrom("/html/timeout/index2.html"))
                .withChunkedDribbleDelay(15, 30 * 1_000)

        ));

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
        config.setConnectionTimeout(20 * 1_000);

        BasicURLNormalizer normalizer = TestUtils.newNormalizer();
        PageFetcher pageFetcher = new PageFetcher(config, normalizer);
        WebURLFactory webURLFactory = new SleepycatWebURLFactory();
        RobotstxtServer robotstxtServer = new RobotstxtServer(new RobotstxtConfig(), pageFetcher, webURLFactory);
        CrawlController controller = new CrawlController(config, normalizer, pageFetcher, robotstxtServer, TestUtils.createFrontierConfiguration(config));
        controller.addSeed("http://localhost:" + wm.getPort() + "/some/index.html");

        controller.start(VisitAllCrawler.class, 1);


        // then:
        Page p = (Page) controller.getCrawlersLocalData().get(0);
    		Assertions.assertThat(p.getStatusCode()).isEqualTo(200);
				Assertions.assertThat(p.getFetchResponseHeaders()).hasSizeGreaterThan(1);
    }
}

class VisitAllCrawler extends WebCrawler {
	
	private Page page;
	
	@Override
	protected void onContentFetchError(Page page, Exception e) {
		this.page = page;
		super.onContentFetchError(page, e);
	}
	
	@Override
	protected void onUnhandledException(Page page, Throwable e) {
		this.page = page;
		super.onUnhandledException(page, e);
	}
	
	@Override
	public Object getMyLocalData() {
		return page;
	}
}
