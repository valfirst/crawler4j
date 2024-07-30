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
package de.hshn.mi.crawler4j.frontier;

import de.hshn.mi.crawler4j.url.URLFrontierWebURLFactory;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.frontier.DocIDServer;
import edu.uci.ics.crawler4j.frontier.Frontier;
import edu.uci.ics.crawler4j.frontier.FrontierConfiguration;
import edu.uci.ics.crawler4j.url.WebURLFactory;

public class URLFrontierConfiguration implements FrontierConfiguration {

    private final URLFrontierAdapter adapter;

    public URLFrontierConfiguration(CrawlConfig crawlConfig, int maxQueues, String hostname, int port) {
        this.adapter = new URLFrontierAdapter(crawlConfig, maxQueues, hostname, port);
    }

    @Override
    public DocIDServer getDocIDServer() {
        return adapter;
    }

    @Override
    public Frontier getFrontier() {
        return adapter;
    }

    @Override
    public WebURLFactory getWebURLFactory() {
        return new URLFrontierWebURLFactory();
    }

    @Override
    public void close() {
        //nothing to do
    }
}
