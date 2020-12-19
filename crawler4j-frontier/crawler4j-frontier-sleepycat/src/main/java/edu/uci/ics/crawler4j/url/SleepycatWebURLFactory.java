package edu.uci.ics.crawler4j.url;

public class SleepycatWebURLFactory implements WebURLFactory {
    @Override
    public WebURL newWebUrl() {
        return new WebURLImpl();
    }
}
