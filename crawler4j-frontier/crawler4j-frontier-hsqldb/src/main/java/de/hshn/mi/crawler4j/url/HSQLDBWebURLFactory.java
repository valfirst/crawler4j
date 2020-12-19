package de.hshn.mi.crawler4j.url;

import edu.uci.ics.crawler4j.url.WebURL;
import edu.uci.ics.crawler4j.url.WebURLFactory;

public class HSQLDBWebURLFactory implements WebURLFactory {
    @Override
    public WebURL newWebUrl() {
        return new HSQLDBWebURLImpl();
    }
}
