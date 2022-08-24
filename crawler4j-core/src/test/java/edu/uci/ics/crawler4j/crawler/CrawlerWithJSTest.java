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
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

class CrawlerWithJSTest {
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
                .withBody(TestUtils.getInputStringFrom("/html/js/index.html")))
              );

        // and: "a page with js in the head tag"
        wm.stubFor(get(urlPathMatching("/some/page1.html"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/html")
                .withBody(TestUtils.getInputStringFrom("/html/js/page1.html")))
              );

        // and: "a page with js in the head tag and a script src in the body"
        wm.stubFor(get(urlPathMatching("/some/page2.html"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/html")
                .withBody(TestUtils.getInputStringFrom("/html/js/page2.html")))
              );
        wm.stubFor(get(urlPathMatching("/js/app.js"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/javascript")
                .withBody(TestUtils.getInputStringFrom("/html/js/app.js")))
              );
        wm.stubFor(get(urlPathMatching("/js/module1.js"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/javascript")
                .withBody(TestUtils.getInputStringFrom("/html/js/module1.js")))
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

        controller.start(ShouldWebCrawler.class, 1);

        // then: "java script files must be visited"
        wm.verify(exactly(1), getRequestedFor(urlEqualTo("/robots.txt")));
        wm.verify(exactly(1), getRequestedFor(urlEqualTo("/js/app.js")));
        wm.verify(exactly(1), getRequestedFor(urlEqualTo("/js/module1.js")));
    }
}

class ShouldWebCrawler extends WebCrawler {

}
