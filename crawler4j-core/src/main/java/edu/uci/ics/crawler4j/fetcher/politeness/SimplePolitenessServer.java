package edu.uci.ics.crawler4j.fetcher.politeness;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.url.WebURL;

import java.util.Date;

/**
 * This class handles the politeness logic similar to earlier versions of crawler4j, i.e. apply a delay to every request.
 */
public class SimplePolitenessServer implements edu.uci.ics.crawler4j.PolitenessServer {

    protected CrawlConfig config;
    private final Object mutex = new Object();
    protected long lastFetchTime = 0;

    public SimplePolitenessServer(CrawlConfig config) {
        this.config = config;
    }

    @Override
    public long applyPoliteness(WebURL url) {
        synchronized (mutex) {
            long now = new Date().getTime();
            if (now - this.lastFetchTime < config.getPolitenessDelay()) {
                return config.getPolitenessDelay() - (now - this.lastFetchTime);
            }
            this.lastFetchTime = new Date().getTime();

            return NO_POLITENESS_APPLIED;
        }
    }
}
