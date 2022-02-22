package edu.uci.ics.crawler4j;

import edu.uci.ics.crawler4j.url.WebURL;

public interface PolitenessServer {


    int NO_POLITENESS_APPLIED = -1;

    /**
     * @param url must not be {@code null}
     * @return the delay in ms or ${@code NO_POLITENESS_APPLIED} (if no politeness needs to be applied)
     */
    long applyPoliteness(WebURL url);

}
