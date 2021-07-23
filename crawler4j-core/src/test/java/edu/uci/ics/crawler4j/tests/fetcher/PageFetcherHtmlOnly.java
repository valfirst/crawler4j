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

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import edu.uci.ics.crawler4j.url.WebURL;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.exceptions.PageBiggerThanMaxSizeException;
import edu.uci.ics.crawler4j.fetcher.PageFetchResult;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;

public class PageFetcherHtmlOnly extends PageFetcher {

    public PageFetcherHtmlOnly(CrawlConfig config)
            throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
        super(config);
    }

    @Override
    public PageFetchResult fetchPage(WebURL webUrl)
            throws InterruptedException, IOException, PageBiggerThanMaxSizeException, URISyntaxException {
        String toFetchURL = webUrl.getURL();

        PageFetchResult fetchResult = new PageFetchResult(config.isHaltOnError());
        HttpHead head = null;
        try {
            head = new HttpHead(toFetchURL);

            synchronized (mutex) {
                long now = new Date().getTime();
                if (now - this.lastFetchTime < getConfig().getPolitenessDelay()) {
                    Thread.sleep(getConfig().getPolitenessDelay() - (now - this.lastFetchTime));
                }
                this.lastFetchTime = new Date().getTime();
            }

            CloseableHttpResponse response = httpClient.execute(head);

            fetchResult.setEntity(response.getEntity());
            fetchResult.setResponseHeaders(response.getHeaders());
            fetchResult.setFetchedUrl(toFetchURL);
            fetchResult.setStatusCode(response.getCode());

            String contentType = response.containsHeader("Content-Type") ?
                                 response.getFirstHeader("Content-Type").getValue() : null;
            String typeStr = (contentType != null) ? contentType.toLowerCase() : "";

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
