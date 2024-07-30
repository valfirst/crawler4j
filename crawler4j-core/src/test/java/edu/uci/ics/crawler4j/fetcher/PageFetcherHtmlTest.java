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
package edu.uci.ics.crawler4j.fetcher;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.url.WebURLImpl;

public class PageFetcherHtmlTest {

    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
        .options(new WireMockConfiguration().dynamicPort())
        .build();

    @Test
    public void testCustomPageFetcher() throws Exception {

        wm.stubFor(WireMock.head(WireMock.urlEqualTo("/some/index.html"))
                                 .willReturn(WireMock.aResponse()
                                                     .withStatus(200)
                                                     .withHeader("Content-Type", "text/html")));

        wm.stubFor(WireMock.get(WireMock.urlEqualTo("/some/index.html"))
                                 .willReturn(WireMock.aResponse()
                                                     .withStatus(200)
                                                     .withHeader("Content-Type", "text/html")
                                                     .withHeader("Content-Length", "47")
                                                     .withBody("<html><body><h1>this is " +
                                                               "html</h1></body></html>")));

        wm.stubFor(WireMock.get(WireMock.urlEqualTo("/some/invoice.pdf"))
                                 .willReturn(WireMock.aResponse()
                                                     .withStatus(200)
                                                     .withHeader("Content-Type", "application/pdf")
                                                     .withBody(new byte[] {1, 2, 3, 4})));

        wm.stubFor(WireMock.head(WireMock.urlEqualTo("/some/invoice.pdf"))
                                 .willReturn(WireMock.aResponse()
                                                     .withStatus(200)
                                                     .withHeader("Content-Type",
                                                                 "application/pdf")));

        CrawlConfig cfg = new CrawlConfig();
        WebURLImpl url = new WebURLImpl();

        url.setURL("http://localhost:" + wm.getPort() + "/some/index.html");
        PageFetcher pf = new PageFetcherHtmlOnly(cfg);
        pf.fetchPage(url).fetchContent(new Page(url), 47);

        wm.verify(1, WireMock.headRequestedFor(WireMock.urlEqualTo("/some/index.html")));
        wm.verify(1, WireMock.getRequestedFor(WireMock.urlEqualTo("/some/index.html")));

        url.setURL("http://localhost:" + wm.getPort() + "/some/invoice.pdf");
        pf = new PageFetcherHtmlOnly(cfg);
        pf.fetchPage(url).fetchContent(new Page(url), 4);

        wm.verify(1, WireMock.headRequestedFor(WireMock.urlEqualTo("/some/invoice.pdf")));
        wm.verify(0, WireMock.getRequestedFor(WireMock.urlEqualTo("/some/invoice.pdf")));
    }
}
