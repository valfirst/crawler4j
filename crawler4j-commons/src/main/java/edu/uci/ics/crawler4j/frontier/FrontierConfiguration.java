package edu.uci.ics.crawler4j.frontier;

import edu.uci.ics.crawler4j.url.WebURLFactory;

public interface FrontierConfiguration {

    DocIDServer getDocIDServer();

    Frontier getFrontier();

    WebURLFactory getWebURLFactory();

    void close();
}
