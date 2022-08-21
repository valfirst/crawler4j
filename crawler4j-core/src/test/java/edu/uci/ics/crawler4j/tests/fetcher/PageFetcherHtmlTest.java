/*-
 * #%L
 * de.hs-heilbronn.mi:crawler4j-core
 * %%
 * Copyright (C) 2010 - 2021 crawler4j-fork (pre-fork: Yasser Ganjisaffar)
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
package edu.uci.ics.crawler4j.tests.fetcher;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.url.WebURLImpl;

public class PageFetcherHtmlTest {

    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
        .options(new WireMockConfiguration().dynamicPort())
        .build();

    @Test
    public void testCustomPageFetcher() throws Exception {

        WireMock.stubFor(WireMock.head(WireMock.urlEqualTo("/some/index.html"))
                                 .willReturn(WireMock.aResponse()
                                                     .withStatus(200)
                                                     .withHeader("Content-Type", "text/html")));

        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/some/index.html"))
                                 .willReturn(WireMock.aResponse()
                                                     .withStatus(200)
                                                     .withHeader("Content-Type", "text/html")
                                                     .withHeader("Content-Length", "47")
                                                     .withBody("<html><body><h1>this is " +
                                                               "html</h1></body></html>")));

        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/some/invoice.pdf"))
                                 .willReturn(WireMock.aResponse()
                                                     .withStatus(200)
                                                     .withHeader("Content-Type", "application/pdf")
                                                     .withBody(new byte[] {1, 2, 3, 4})));

        WireMock.stubFor(WireMock.head(WireMock.urlEqualTo("/some/invoice.pdf"))
                                 .willReturn(WireMock.aResponse()
                                                     .withStatus(200)
                                                     .withHeader("Content-Type",
                                                                 "application/pdf")));

        CrawlConfig cfg = new CrawlConfig();
        WebURLImpl url = new WebURLImpl();

        url.setURL("http://localhost:" + wm.getPort() + "/some/index.html");
        PageFetcher pf = new PageFetcherHtmlOnly(cfg);
        pf.fetchPage(url).fetchContent(new Page(url), 47);

        WireMock.verify(1, WireMock.headRequestedFor(WireMock.urlEqualTo("/some/index.html")));
        WireMock.verify(1, WireMock.getRequestedFor(WireMock.urlEqualTo("/some/index.html")));

        url.setURL("http://localhost:" + wm.getPort() + "/some/invoice.pdf");
        pf = new PageFetcherHtmlOnly(cfg);
        pf.fetchPage(url).fetchContent(new Page(url), 4);

        WireMock.verify(1, WireMock.headRequestedFor(WireMock.urlEqualTo("/some/invoice.pdf")));
        WireMock.verify(0, WireMock.getRequestedFor(WireMock.urlEqualTo("/some/invoice.pdf")));
    }
}
