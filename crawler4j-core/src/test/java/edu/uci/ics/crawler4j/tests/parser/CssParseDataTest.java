package edu.uci.ics.crawler4j.tests.parser;

import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.uci.ics.crawler4j.parser.CssParseData;
import edu.uci.ics.crawler4j.test.Crawler4jTestUtils;
import edu.uci.ics.crawler4j.test.TestUtils;
import edu.uci.ics.crawler4j.url.WebURL;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CssParseDataTest {

    /**
     * REMARK: NO URLS SHOULD BE FOUND.
     * <p>
     * The problem is that the pattern inside CssParseData will match parts of data urls instead of the complete url.
     * More specifically, following will get matched
     * by the expression for "url(...)" instead of
     * by the expression for "url("...")":
     * url("...)...")
     * <p>
     * A possible solution is to later on inside the extractUrlInCssText()-method instead of:
     * if (url == null || url.startsWith("data:")) {
     * also test for incomplete matches with:
     * if (url == null || StringUtils.startsWithAny(url, "data:", "'data:", "\"data:")) {
     * (org.apache.commons.lang3.StringUtils)
     */
    @Test
    void extractUrlInCssTextIgnoresDataUrlsFromBootstrapMinCssTest() {
        final String cssText = TestUtils.getInputStringFrom("/css/bootstrap.min.css");
        assertNoURLsFoundInCSS(cssText);
    }

    /**
     * REMARK: NO URLS SHOULD BE FOUND.
     */
    @Test
    void extractUrlInCssTextIgnoresDataUrlFromBootstrapSubsetTest() {
        final String cssText = TestUtils.getInputStringFrom("/css/bootstrap.min-subset.css");
        assertNoURLsFoundInCSS(cssText);
    }

    private void assertNoURLsFoundInCSS(final String cssText) {
        final WebURL webURL = Crawler4jTestUtils.newWebURL("http://www.test.com/path/to/bootstrap.min.css");

        final CssParseData cssParseData = Crawler4jTestUtils.newCssParseData();
        cssParseData.setTextContent(cssText);
        cssParseData.setOutgoingUrls(webURL);
        final Set<WebURL> outgoingUrls = cssParseData.getOutgoingUrls();
        assertNotNull(outgoingUrls);
        Assertions.assertThat(outgoingUrls).isEmpty();
    }
}
