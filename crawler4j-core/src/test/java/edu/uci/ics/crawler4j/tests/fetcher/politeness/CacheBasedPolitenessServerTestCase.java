package edu.uci.ics.crawler4j.tests.fetcher.politeness;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.fetcher.politeness.CachedPolitenessServer;
import edu.uci.ics.crawler4j.url.AbstractWebURL;
import edu.uci.ics.crawler4j.url.WebURL;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CacheBasedPolitenessServerTestCase {

    private CachedPolitenessServer cacheBasedPolitenessServer;
    private CrawlConfig config;

    @Before
    public void init() {
        this.config = new CrawlConfig();
        this.config.setPolitenessDelay(100);
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

        //let's wait some time, it should not be listed anymore
        sleep(1000);

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

        //let's wait some time, it should not be listed anymore
        sleep(3000);

        politenessDelay = cacheBasedPolitenessServer.applyPoliteness(webUrl);

        assertEquals(CachedPolitenessServer.NO_POLITENESS_APPLIED, politenessDelay);

    }

    @Test
    public void testRemoveExpiredEntries() {

        WebURL webUrl = new MockWebUrl();
        webUrl.setURL("https://github.com/yasserg/crawler4j");

        long politenessDelay = cacheBasedPolitenessServer.applyPoliteness(webUrl);

        assertEquals(CachedPolitenessServer.NO_POLITENESS_APPLIED, politenessDelay);

        webUrl.setURL("http://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ConcurrentLinkedQueue.html");

        politenessDelay = cacheBasedPolitenessServer.applyPoliteness(webUrl);

        assertEquals(CachedPolitenessServer.NO_POLITENESS_APPLIED, politenessDelay);

        webUrl.setURL("https://www.google.de/?gws_rd=ssl");

        politenessDelay = cacheBasedPolitenessServer.applyPoliteness(webUrl);

        assertEquals(CachedPolitenessServer.NO_POLITENESS_APPLIED, politenessDelay);

        webUrl.setURL("https://stackoverflow.com/");

        politenessDelay = cacheBasedPolitenessServer.applyPoliteness(webUrl);

        assertEquals(CachedPolitenessServer.NO_POLITENESS_APPLIED, politenessDelay);

        //let's wait some time, it should not be listed anymore
        sleep(5000);

        //entries should be evicted...
        assertEquals(0, cacheBasedPolitenessServer.getSize());

    }


    private void sleep(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            //nothing to do here
        }
    }

    public class MockWebUrl extends AbstractWebURL {

    }


}