package edu.uci.ics.crawler4j.frontier;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.url.WebURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public interface Frontier {

    public void scheduleAll(List<WebURL> urls);

    public void schedule(WebURL url);

    public void getNextURLs(int max, List<WebURL> result);

    public void setProcessed(WebURL webURL);

    public long getQueueLength();

    public long getNumberOfAssignedPages();

    public long getNumberOfProcessedPages();

    public long getNumberOfScheduledPages();

    public boolean isFinished();

    public void close();
    
    public void finish();

}
