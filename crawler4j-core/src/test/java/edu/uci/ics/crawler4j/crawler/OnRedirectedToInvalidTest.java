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
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class OnRedirectedToInvalidTest {

    @TempDir
  public File crawlStorageFolder;

    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
        .options(new WireMockConfiguration().dynamicPort())
        .build();


    @ValueSource(ints = {300, 301, 302, 303, 307, 308})
    @ParameterizedTest
    void interceptRedirectToInvalidUrl(int redirectHttpCode) throws Exception {
        // given: "an index page with links to a redirect"

        String redirectToNothing = "asd://-invalid-/varybadlocation";

    		wm.stubFor(get(urlEqualTo("/some/index.html"))
            .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "text/html")
            .withBody(TestUtils.getInputStringFrom("/html/redirectToNothing/index.html")))
          );

        // when: "the redirect point to an invalid url"
    		wm.stubFor(get(urlPathMatching("/some/redirect.html"))
            .willReturn(aResponse()
            .withStatus(redirectHttpCode)
            .withHeader("Content-Type", "text/html")
            .withHeader("Location", redirectToNothing)
            .withBody(TestUtils.getInputStringFrom("/html/redirectToNothing/redirect.html")))
          );

        // and:
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
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        robotstxtConfig.setEnabled(false);
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher, webURLFactory);
        CrawlController controller = new CrawlController(config, normalizer, pageFetcher, robotstxtServer, TestUtils.createFrontierConfiguration(config));
        controller.addSeed("http://localhost:" + wm.getPort() + "/some/index.html");

        HandleInvalidRedirectWebCrawler crawler = new HandleInvalidRedirectWebCrawler();
        controller.start(crawler);

        // then: "the right event must triggered"
        Assertions.assertThat(crawler.invalidLocation).isEqualTo("/some/redirect.html");
    }
}

class HandleInvalidRedirectWebCrawler extends WebCrawler {

    String invalidLocation;

    @Override
		protected void onRedirectedToInvalidUrl(Page page) {
        super.onRedirectedToInvalidUrl(page);
        invalidLocation = page.getWebURL().getPath();
    }

}
