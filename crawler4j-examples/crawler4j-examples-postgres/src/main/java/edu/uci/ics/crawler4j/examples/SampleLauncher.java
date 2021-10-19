/*-
 * #%L
 * de.hs-heilbronn.mi:crawler4j-examples-postgres
 * %%
 * Copyright (C) 2010 - 2021 crawler4j-fork (pre-fork: Yasser Ganjisaffar)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package edu.uci.ics.crawler4j.examples;

import crawlercommons.filters.basic.BasicURLNormalizer;
import edu.uci.ics.crawler4j.frontier.FrontierConfiguration;
import edu.uci.ics.crawler4j.frontier.SleepycatFrontierConfiguration;
import edu.uci.ics.crawler4j.url.SleepycatWebURLFactory;
import org.flywaydb.core.Flyway;

import com.google.common.io.Files;
import com.mchange.v2.c3p0.ComboPooledDataSource;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.examples.crawler.PostgresCrawlerFactory;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class SampleLauncher {

    public static void main(String[] args) throws Exception {

        String crawlStorageFolder = Files.createTempDir().getAbsolutePath();
        final int numberOfCrawlers = Integer.parseInt(args[2]);

        CrawlConfig config = new CrawlConfig();

        config.setPolitenessDelay(100);

        config.setCrawlStorageFolder(crawlStorageFolder);

        config.setMaxPagesToFetch(Integer.parseInt(args[0]));

        /*
         * Instantiate the controller for this crawl.
         */

        BasicURLNormalizer normalizer = BasicURLNormalizer.newBuilder().idnNormalization(BasicURLNormalizer.IdnNormalization.NONE).build();
        PageFetcher pageFetcher = new PageFetcher(config, normalizer);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        FrontierConfiguration frontierConfiguration = new SleepycatFrontierConfiguration(config);
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher, new SleepycatWebURLFactory());
        CrawlController controller = new CrawlController(config, normalizer, pageFetcher, robotstxtServer, frontierConfiguration);

        /*
         * For each crawl, you need to add some seed urls. These are the first
         * URLs that are fetched and then the crawler starts following links
         * which are found in these pages
         */
        controller.addSeed("https://de.wikipedia.org/wiki/Java_Database_Connectivity");
        controller.addSeed("https://de.wikipedia.org/wiki/Relationale_Datenbank");
        controller.addSeed("https://pt.wikipedia.org/wiki/JDBC");
        controller.addSeed("https://pt.wikipedia.org/wiki/Protocolo");
        controller.addSeed("https://de.wikipedia.org/wiki/Datenbank");

        Flyway flyway = Flyway.configure().dataSource(args[1], "postgres", "postgres").load();
        flyway.migrate();

        ComboPooledDataSource pool = new ComboPooledDataSource();
        pool.setDriverClass("org.postgresql.Driver");
        pool.setJdbcUrl(args[1]);
        pool.setUser("postgres");
        pool.setPassword("postgres");
        pool.setMaxPoolSize(numberOfCrawlers);
        pool.setMinPoolSize(numberOfCrawlers);
        pool.setInitialPoolSize(numberOfCrawlers);

        /*
         * Start the crawl. This is a blocking operation, meaning that your code
         * will reach the line after this only when crawling is finished.
         */
        controller.start(new PostgresCrawlerFactory(pool), numberOfCrawlers);

        pool.close();
    }

}
