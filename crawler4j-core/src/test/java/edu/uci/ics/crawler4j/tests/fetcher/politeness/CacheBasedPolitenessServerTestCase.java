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
package edu.uci.ics.crawler4j.tests.fetcher.politeness;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.fetcher.politeness.CachedPolitenessServer;
import edu.uci.ics.crawler4j.url.AbstractWebURL;
import edu.uci.ics.crawler4j.url.WebURL;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Callable;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CacheBasedPolitenessServerTestCase {

    private CachedPolitenessServer cacheBasedPolitenessServer;

    @Before
    public void init() {
        CrawlConfig config = new CrawlConfig();
        config.setPolitenessDelay(100);
        this.cacheBasedPolitenessServer = new CachedPolitenessServer(config);
    }

    @Test
    public void testApplyPoliteness1() {

        WebURL webUrl = new MockWebUrl();
        webUrl.setURL("https://github.com/yasserg/crawler4j");

        long politenessDelay = cacheBasedPolitenessServer.applyPoliteness(webUrl);

        assertEquals(CachedPolitenessServer.NO_POLITENESS_APPLIED, politenessDelay);

        politenessDelay = cacheBasedPolitenessServer.applyPoliteness(webUrl);

        assertTrue(politenessDelay > 0);

    }

    @Test
    public void testApplyPoliteness2() {

        WebURL webUrl = new MockWebUrl();
        webUrl.setURL("https://github.com/yasserg/crawler4j");

        long politenessDelay = cacheBasedPolitenessServer.applyPoliteness(webUrl);

        assertEquals(CachedPolitenessServer.NO_POLITENESS_APPLIED, politenessDelay);

        webUrl.setURL("https://github.com/yasserg/crawler4j/blob/master/pom.xml");

        politenessDelay = cacheBasedPolitenessServer.applyPoliteness(webUrl);

        assertTrue(politenessDelay > 0);

        await().atMost(5, SECONDS).until(cacheIsEvicted());

        politenessDelay = cacheBasedPolitenessServer.applyPoliteness(webUrl);

        assertEquals(CachedPolitenessServer.NO_POLITENESS_APPLIED, politenessDelay);

    }

    @Test
    public void testApplyPoliteness3() {

        WebURL webUrl = new MockWebUrl();
        webUrl.setURL("https://github.com/yasserg/crawler4j");

        long politenessDelay = cacheBasedPolitenessServer.applyPoliteness(webUrl);

        assertEquals(CachedPolitenessServer.NO_POLITENESS_APPLIED, politenessDelay);

        webUrl.setURL("http://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ConcurrentLinkedQueue.html");

        politenessDelay = cacheBasedPolitenessServer.applyPoliteness(webUrl);

        assertEquals(CachedPolitenessServer.NO_POLITENESS_APPLIED, politenessDelay);

        webUrl.setURL("https://github.com/yasserg/crawler4j/blob/master/pom.xml");

        politenessDelay = cacheBasedPolitenessServer.applyPoliteness(webUrl);

        assertTrue(politenessDelay > 0);

        await().atMost(5, SECONDS).until(cacheIsEvicted());

        politenessDelay = cacheBasedPolitenessServer.applyPoliteness(webUrl);

        assertEquals(CachedPolitenessServer.NO_POLITENESS_APPLIED, politenessDelay);

    }

    private Callable<Boolean> cacheIsEvicted() {
        return () -> 0 == cacheBasedPolitenessServer.getSize();
    }

    public static class MockWebUrl extends AbstractWebURL {

    }

}
