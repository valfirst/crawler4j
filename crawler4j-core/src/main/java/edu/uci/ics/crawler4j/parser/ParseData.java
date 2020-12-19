package edu.uci.ics.crawler4j.parser;

import java.util.Set;

import edu.uci.ics.crawler4j.url.WebURL;

public interface ParseData {

    Set<WebURL> getOutgoingUrls();

    void setOutgoingUrls(Set<WebURL> outgoingUrls);

    @Override
    String toString();
}