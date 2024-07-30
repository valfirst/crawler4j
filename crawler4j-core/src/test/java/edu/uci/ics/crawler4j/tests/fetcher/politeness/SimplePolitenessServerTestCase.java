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
package edu.uci.ics.crawler4j.tests.fetcher.politeness;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.fetcher.politeness.CachedPolitenessServer;
import edu.uci.ics.crawler4j.fetcher.politeness.SimplePolitenessServer;
import edu.uci.ics.crawler4j.test.SimpleWebURL;
import edu.uci.ics.crawler4j.url.WebURL;

public class SimplePolitenessServerTestCase {

    private edu.uci.ics.crawler4j.PolitenessServer simplePolitenessServer;
    private CrawlConfig config;

    @BeforeEach
    public void init() {
        this.config = new CrawlConfig();
        this.config.setPolitenessDelay(100);
        this.simplePolitenessServer = new SimplePolitenessServer(config);
    }

    @Test
    public void testApplyPoliteness1() {

        WebURL webUrl = new SimpleWebURL();
        webUrl.setURL("https://github.com/yasserg/crawler4j");

        long politenessDelay = simplePolitenessServer.applyPoliteness(webUrl);

        Assertions.assertThat(politenessDelay).isEqualTo(CachedPolitenessServer.NO_POLITENESS_APPLIED);

        politenessDelay = simplePolitenessServer.applyPoliteness(webUrl);

        Assertions.assertThat(politenessDelay).isBetween(//
        		Long.valueOf(config.getPolitenessDelay() - 10)//
        		, Long.valueOf(config.getPolitenessDelay()));

    }

    @Test
    public void testApplyPoliteness2() {

        WebURL webUrl = new SimpleWebURL();
        webUrl.setURL("https://github.com/yasserg/crawler4j");

        long politenessDelay = simplePolitenessServer.applyPoliteness(webUrl);

        Assertions.assertThat(politenessDelay).isEqualTo(CachedPolitenessServer.NO_POLITENESS_APPLIED);

        webUrl.setURL("https://github.com/yasserg/crawler4j/blob/master/pom.xml");

        politenessDelay = simplePolitenessServer.applyPoliteness(webUrl);

        Assertions.assertThat(politenessDelay).isBetween(//
        		Long.valueOf(config.getPolitenessDelay() - 10)//
        		, Long.valueOf(config.getPolitenessDelay()));

        //let's wait some time, it should not be listed anymore
        sleep(1000);

        politenessDelay = simplePolitenessServer.applyPoliteness(webUrl);

        Assertions.assertThat(politenessDelay).isEqualTo(CachedPolitenessServer.NO_POLITENESS_APPLIED);

    }

    @Test
    public void testApplyPoliteness3() {

        WebURL webUrl = new SimpleWebURL();
        webUrl.setURL("https://github.com/yasserg/crawler4j");

        long politenessDelay = simplePolitenessServer.applyPoliteness(webUrl);

        Assertions.assertThat(politenessDelay).isEqualTo(CachedPolitenessServer.NO_POLITENESS_APPLIED);

        webUrl.setURL("http://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ConcurrentLinkedQueue.html");

        politenessDelay = simplePolitenessServer.applyPoliteness(webUrl);

        Assertions.assertThat(politenessDelay).isBetween(//
        		Long.valueOf(config.getPolitenessDelay() - 10)//
        		, Long.valueOf(config.getPolitenessDelay()));

        webUrl.setURL("https://github.com/yasserg/crawler4j/blob/master/pom.xml");

        politenessDelay = simplePolitenessServer.applyPoliteness(webUrl);

        Assertions.assertThat(politenessDelay).isEqualTo(config.getPolitenessDelay());

        //let's wait some time, it should not be listed anymore
        sleep(3000);

        politenessDelay = simplePolitenessServer.applyPoliteness(webUrl);

        Assertions.assertThat(politenessDelay).isEqualTo(CachedPolitenessServer.NO_POLITENESS_APPLIED);

    }

    private void sleep(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            //nothing to do here
        }
    }

}
