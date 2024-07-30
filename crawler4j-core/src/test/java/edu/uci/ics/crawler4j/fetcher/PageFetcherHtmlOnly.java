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

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;

import edu.uci.ics.crawler4j.PolitenessServer;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.exceptions.PageBiggerThanMaxSizeException;
import edu.uci.ics.crawler4j.fetcher.politeness.SimplePolitenessServer;
import edu.uci.ics.crawler4j.test.TestUtils;
import edu.uci.ics.crawler4j.url.WebURL;

public class PageFetcherHtmlOnly extends PageFetcher {

    public PageFetcherHtmlOnly(CrawlConfig config)
            throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
        super(config, TestUtils.newNormalizer(), new SimplePolitenessServer(config));
    }

    @Override
    public PageFetchResult fetchPage(WebURL webUrl)
            throws InterruptedException, IOException, PageBiggerThanMaxSizeException, URISyntaxException {
        String toFetchURL = webUrl.getURL();

        PageFetchResult fetchResult = new PageFetchResult(config.isHaltOnError());
        HttpHead head = null;
        try {
            head = new HttpHead(toFetchURL);

            final long politenessDelay = getPolitenessServer().applyPoliteness(webUrl);
            if (politenessDelay != PolitenessServer.NO_POLITENESS_APPLIED) {
                Thread.sleep(politenessDelay);
            }

            CloseableHttpResponse response = httpClient.execute(head);

            fetchResult.setEntity(response.getEntity());
            fetchResult.setResponseHeaders(response.getHeaders());
            fetchResult.setFetchedUrl(toFetchURL);
            fetchResult.setStatusCode(response.getCode());

            String contentType = response.containsHeader("Content-Type") ?
                    response.getFirstHeader("Content-Type").getValue() : null;
            String typeStr = (contentType != null) ? contentType.toLowerCase(Locale.ROOT) : "";

            if (typeStr.equals("") || (typeStr.contains("text") && typeStr.contains("html"))) {
                return super.fetchPage(webUrl);
            } else {
                return fetchResult;
            }
        } finally {
            if (head != null) {
                head.abort();
            }
        }
    }
}
