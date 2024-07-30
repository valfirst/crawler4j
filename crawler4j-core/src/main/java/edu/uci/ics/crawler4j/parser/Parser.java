/*
 * Copyright 2010-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.uci.ics.crawler4j.parser;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.Validate;

import crawlercommons.filters.basic.BasicURLNormalizer;
import edu.uci.ics.crawler4j.Constants;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.url.TLDList;
import edu.uci.ics.crawler4j.url.WebURLFactory;
import edu.uci.ics.crawler4j.util.Net;
import edu.uci.ics.crawler4j.util.Util;

/**
 * @author Yasser Ganjisaffar
 */
public class Parser {

    private final CrawlConfig config;

    private final HtmlParser htmlContentParser;

    private TikaLanguageDetector languageDetector;

    private final Net net;
    private final WebURLFactory factory;
    private final BasicURLNormalizer normalizer;

    public Parser(CrawlConfig config, BasicURLNormalizer normalizer, TLDList tldList, WebURLFactory webURLFactory) throws IOException {
        this(config, normalizer, new TikaHtmlParser(config, normalizer, tldList, webURLFactory), tldList, webURLFactory);
    }

    public Parser(CrawlConfig config, BasicURLNormalizer normalizer, HtmlParser htmlParser, TLDList tldList, WebURLFactory webURLFactory) throws IOException {
        this.config = config;
        this.htmlContentParser = htmlParser;
        this.net = new Net(config, tldList, webURLFactory);
        this.factory = webURLFactory;
        this.normalizer = normalizer;
        if (config.isLanguageDetection()) {
            this.languageDetector = new TikaLanguageDetector();
        }
    }

    public void parse(Page page) throws Exception {
        if (Util.hasBinaryContent(page.getContentType())) { // BINARY

            if (!config.isIncludeBinaryContentInCrawling()) {
                throw new NotAllowedContentException();
            }

            BinaryParseData parseData = createBinaryParseData();
            if (config.isProcessBinaryContentInCrawling()) {
                parseData.parseBinaryContentAndSetHtml(page);
            } else {
                parseData.setHtml(Constants.EMPTY_HTML_TAGS);
            }

            String html = parseData.getHtml();
            Validate.validState(html != null//
                    , "BinaryParseData.parseBinaryContentAndSetHtml(...) should initialize the html value");
            parseData.setOutgoingUrls(net.extractUrls(html));
            page.setParseData(parseData);

        } else if (Util.hasCssTextContent(page.getContentType())) { // text/css

            CssParseData parseData = createCssParseData();
            setTextContent(parseData, page);
            parseData.parseAndSetOutgoingUrls(page);
            page.setParseData(parseData);

        } else if (Util.hasPlainTextContent(page.getContentType())) { // plain Text

            TextParseData parseData = createTextParseData();
            setTextContent(parseData, page);
            // Allow the same "moment" to parse its content with the Page-object as context.
            parseData.parseAndSetOutgoingUrls(page);
            // Behavior kept for backwards compatibility -> also identical to the handling of binary content
            parseData.setOutgoingUrls(net.extractUrls(parseData.getTextContent()));
            page.setParseData(parseData);

        } else { // isHTML

            HtmlParseData parsedData = createHtmlParseData(page);

            if (page.getContentCharset() == null) {
                page.setContentCharset(parsedData.getContentCharset());
            }

            if (config.isLanguageDetection()) {
                // Please note that identifying language takes less than 10 milliseconds
                page.setLanguage(languageDetector.detect(parsedData.getText()));
            } else {
                page.setLanguage("");
            }

            page.setParseData(parsedData);
        }
    }

    private void setTextContent(TextParseData parseData, Page page)
            throws UnsupportedEncodingException {
        if (page.getContentCharset() == null) {
            parseData.setTextContent(new String(page.getContentData(), StandardCharsets.UTF_8));
        } else {
            parseData.setTextContent(new String(page.getContentData(), page.getContentCharset()));
        }
    }

    /**
     * Open for extension
     */
    protected BinaryParseData createBinaryParseData() {
        return new BinaryParseData();
    }

    /**
     * Open for extension
     */
    protected CssParseData createCssParseData() {
        return new CssParseData(getFactory(), getNormalizer(), getConfig().isHaltOnError());
    }

    /**
     * Open for extension
     */
    protected TextParseData createTextParseData() {
        return new TextParseData();
    }

    /**
     * Open for extension
     */
    protected HtmlParseData createHtmlParseData(final Page page)
            throws Exception {
        return getHtmlContentParser().parse(page);
    }

    protected CrawlConfig getConfig() {
        return config;
    }

    protected WebURLFactory getFactory() {
        return factory;
    }

    protected BasicURLNormalizer getNormalizer() {
        return normalizer;
    }

    protected HtmlParser getHtmlContentParser() {
        return htmlContentParser;
    }
}
