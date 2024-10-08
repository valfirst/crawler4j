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
package edu.uci.ics.crawler4j.crawler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import crawlercommons.filters.basic.BasicURLNormalizer;
import edu.uci.ics.crawler4j.frontier.*;
import edu.uci.ics.crawler4j.url.WebURLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.parser.Parser;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import edu.uci.ics.crawler4j.url.TLDList;
import edu.uci.ics.crawler4j.url.WebURL;

/**
 * The controller that manages a crawling session. This class creates the
 * crawler threads and monitors their progress.
 *
 * @author Yasser Ganjisaffar
 */
public class CrawlController {

    static final Logger logger = LoggerFactory.getLogger(CrawlController.class);
    private final CrawlConfig config;

    /**
     * The 'customData' object can be used for passing custom crawl-related
     * configurations to different components of the crawler.
     */
    protected Object customData;

    /**
     * Once the crawling session finishes the controller collects the local data
     * of the crawler threads and stores them in this List.
     */
    protected List<Object> crawlersLocalData = new ArrayList<>();

    /**
     * Is the crawling of this session finished?
     */
    protected boolean finished;
    private Throwable error;

    /**
     * Is the crawling session set to 'shutdown'. Crawler threads monitor this
     * flag and when it is set they will no longer process new pages.
     */
    protected boolean shuttingDown;

    protected PageFetcher pageFetcher;
    protected RobotstxtServer robotstxtServer;
    protected Frontier frontier;
    protected DocIDServer docIdServer;
    protected TLDList tldList;
    protected WebURLFactory webURLFactory;
    protected BasicURLNormalizer normalizer;

    protected final Object waitingLock = new Object();
    protected final FrontierConfiguration frontierConfiguration;

    protected Parser parser;

    public CrawlController(CrawlConfig config, BasicURLNormalizer normalizer, PageFetcher pageFetcher,
                           RobotstxtServer robotstxtServer, FrontierConfiguration frontierConfiguration) throws Exception {
        this(config, normalizer, pageFetcher, null, robotstxtServer, null, frontierConfiguration);
    }

    public CrawlController(CrawlConfig config, BasicURLNormalizer normalizer, PageFetcher pageFetcher,
                           RobotstxtServer robotstxtServer, TLDList tldList, FrontierConfiguration frontierConfiguration) throws Exception {
        this(config, normalizer, pageFetcher, null, robotstxtServer, tldList, frontierConfiguration);
    }

    public CrawlController(CrawlConfig config, BasicURLNormalizer normalizer, PageFetcher pageFetcher, Parser parser,
                           RobotstxtServer robotstxtServer, TLDList tldList, FrontierConfiguration frontierConfiguration) throws Exception {
        config.validate();
        this.config = config;

        File folder = new File(config.getCrawlStorageFolder());
        if (!folder.exists()) {
            if (folder.mkdirs()) {
                logger.debug("Created folder: " + folder.getAbsolutePath());
            } else {
                throw new Exception(
                        "couldn't create the storage folder: " + folder.getAbsolutePath() +
                                " does it already exist ?");
            }
        }

        this.tldList = tldList == null ? new TLDList(config) : tldList;

        this.frontierConfiguration = frontierConfiguration;
        this.frontier = frontierConfiguration.getFrontier();
        this.docIdServer = frontierConfiguration.getDocIDServer();
        this.webURLFactory = frontierConfiguration.getWebURLFactory();
        this.normalizer = normalizer;

        this.pageFetcher = pageFetcher;
        this.parser = parser == null ? new Parser(config, normalizer, tldList, webURLFactory) : parser;
        this.robotstxtServer = robotstxtServer;

        finished = false;
        shuttingDown = false;

        robotstxtServer.setCrawlConfig(config);
    }

    public Parser getParser() {
        return parser;
    }

    public interface WebCrawlerFactory<T extends WebCrawler> {
        T newInstance() throws Exception;
    }

    private static class SingleInstanceFactory<T extends WebCrawler>
            implements WebCrawlerFactory<T> {

        final T instance;

        SingleInstanceFactory(T instance) {
            this.instance = instance;
        }

        @Override
        public T newInstance() {
            return this.instance;
        }
    }

    private static class DefaultWebCrawlerFactory<T extends WebCrawler>
            implements WebCrawlerFactory<T> {
        final Class<T> clazz;

        DefaultWebCrawlerFactory(Class<T> clazz) {
            this.clazz = clazz;
        }

        @Override
        public T newInstance() throws Exception {
            return clazz.getDeclaredConstructor().newInstance();
        }
    }

    /**
     * Start the crawling session and wait for it to finish.
     * This method utilizes default crawler factory that creates new crawler using Java reflection
     *
     * @param clazz            the class that implements the logic for crawler threads
     * @param numberOfCrawlers the number of concurrent threads that will be contributing in
     *                         this crawling session.
     * @param <T>              Your class extending WebCrawler
     */
    public <T extends WebCrawler> void start(Class<T> clazz, int numberOfCrawlers) {
        this.start(new DefaultWebCrawlerFactory<>(clazz), numberOfCrawlers, true);
    }

    /**
     * Start the crawling session and wait for it to finish.
     * This method depends on a single instance of a crawler. Only that instance will be used for crawling.
     *
     * @param instance the instance of a class that implements the logic for crawler threads
     * @param <T>      Your class extending WebCrawler
     */
    public <T extends WebCrawler> void start(T instance) {
        this.start(new SingleInstanceFactory<>(instance), 1, true);
    }

    /**
     * Start the crawling session and wait for it to finish.
     *
     * @param crawlerFactory   factory to create crawlers on demand for each thread
     * @param numberOfCrawlers the number of concurrent threads that will be contributing in
     *                         this crawling session.
     * @param <T>              Your class extending WebCrawler
     */
    public <T extends WebCrawler> void start(WebCrawlerFactory<T> crawlerFactory,
                                             int numberOfCrawlers) {
        this.start(crawlerFactory, numberOfCrawlers, true);
    }

    /**
     * Start the crawling session and return immediately.
     *
     * @param crawlerFactory   factory to create crawlers on demand for each thread
     * @param numberOfCrawlers the number of concurrent threads that will be contributing in
     *                         this crawling session.
     * @param <T>              Your class extending WebCrawler
     */
    public <T extends WebCrawler> void startNonBlocking(WebCrawlerFactory<T> crawlerFactory,
                                                        final int numberOfCrawlers) {
        this.start(crawlerFactory, numberOfCrawlers, false);
    }

    /**
     * Start the crawling session and return immediately.
     * This method utilizes default crawler factory that creates new crawler using Java reflection
     *
     * @param clazz            the class that implements the logic for crawler threads
     * @param numberOfCrawlers the number of concurrent threads that will be contributing in
     *                         this crawling session.
     * @param <T>              Your class extending WebCrawler
     */
    public <T extends WebCrawler> void startNonBlocking(Class<T> clazz, int numberOfCrawlers) {
        start(new DefaultWebCrawlerFactory<>(clazz), numberOfCrawlers, false);
    }

    protected <T extends WebCrawler> void start(final WebCrawlerFactory<T> crawlerFactory,
                                                final int numberOfCrawlers, boolean isBlocking) {
        try {
            finished = false;
            setError(null);
            crawlersLocalData.clear();
            final List<Thread> threads = new ArrayList<>();
            final List<T> crawlers = new ArrayList<>();

            for (int i = 1; i <= numberOfCrawlers; i++) {
                T crawler = crawlerFactory.newInstance();
                Thread thread = new Thread(crawler, "Crawler " + i);
                crawler.setThread(thread);
                crawler.init(i, this);
                thread.start();
                crawlers.add(crawler);
                threads.add(thread);
                logger.info("Crawler {} started", i);
            }

            final CrawlController controller = this;
            Thread monitorThread = new Thread(() -> {
                try {
                    synchronized (waitingLock) {

                        while (true) {
                            sleep(config.getThreadMonitoringDelaySeconds());
                            boolean someoneIsWorking = false;
                            for (int i = 0; i < threads.size(); i++) {
                                Thread thread = threads.get(i);
                                if (!thread.isAlive()) {
                                    if (!shuttingDown && !config.isHaltOnError()) {
                                        logger.info("Thread {} was dead, I'll recreate it", i);
                                        T crawler = crawlerFactory.newInstance();
                                        thread = new Thread(crawler, "Crawler " + (i + 1));
                                        threads.remove(i);
                                        threads.add(i, thread);
                                        crawler.setThread(thread);
                                        crawler.init(i + 1, controller);
                                        thread.start();
                                        crawlers.remove(i);
                                        crawlers.add(i, crawler);
                                    }
                                } else if (crawlers.get(i).isNotWaitingForNewURLs()) {
                                    someoneIsWorking = true;
                                }
                                Throwable t = crawlers.get(i).getError();
                                if (t != null && config.isHaltOnError()) {
                                    throw new RuntimeException(
                                            "error on thread [" + threads.get(i).getName() + "]", t);
                                }
                            }
                            boolean shutOnEmpty = config.isShutdownOnEmptyQueue();
                            if (!someoneIsWorking && shutOnEmpty) {
                                // Make sure again that none of the threads
                                // are
                                // alive.
                                logger.info(
                                        "It looks like no thread is working, waiting for " +
                                                config.getThreadShutdownDelaySeconds() +
                                                " seconds to make sure...");
                                sleep(config.getThreadShutdownDelaySeconds());

                                someoneIsWorking = false;
                                for (int i = 0; i < threads.size(); i++) {
                                    Thread thread = threads.get(i);
                                    if (thread.isAlive() &&
                                            crawlers.get(i).isNotWaitingForNewURLs()) {
                                        someoneIsWorking = true;
                                    }
                                }
                                if (!someoneIsWorking) {
                                    if (!shuttingDown) {
                                        long queueLength = frontier.getQueueLength();
                                        if (queueLength > 0) {
                                            continue;
                                        }
                                        logger.info(
                                                "No thread is working and no more URLs are in " +
                                                        "queue waiting for another " +
                                                        config.getThreadShutdownDelaySeconds() +
                                                        " seconds to make sure...");
                                        sleep(config.getThreadShutdownDelaySeconds());
                                        queueLength = frontier.getQueueLength();
                                        if (queueLength > 0) {
                                            continue;
                                        }
                                    }

                                    logger.info(
                                            "All of the crawlers are stopped. Finishing the " +
                                                    "process...");
                                    // At this step, frontier notifies the threads that were
                                    // waiting for new URLs and they should stop
                                    frontier.finish();
                                    for (T crawler : crawlers) {
                                        crawler.onBeforeExit();
                                        crawlersLocalData.add(crawler.getMyLocalData());
                                    }

                                    logger.info(
                                            "Waiting for " + config.getCleanupDelaySeconds() +
                                                    " seconds before final clean up...");
                                    sleep(config.getCleanupDelaySeconds());

                                    frontier.close();
                                    docIdServer.close();
                                    pageFetcher.shutDown();

                                    finished = true;
                                    waitingLock.notifyAll();
                                    frontierConfiguration.close();

                                    return;
                                }
                            }
                        }
                    }
                } catch (Throwable e) {
                    if (config.isHaltOnError()) {
                        setError(e);
                        synchronized (waitingLock) {
                            frontier.finish();
                            frontier.close();
                            docIdServer.close();
                            pageFetcher.shutDown();
                            waitingLock.notifyAll();
                            frontierConfiguration.close();
                        }
                    } else {
                        logger.error("Unexpected Error", e);
                    }
                }
            });

            monitorThread.start();

            if (isBlocking) {
                waitUntilFinish();
            }

        } catch (Exception e) {
            if (config.isHaltOnError()) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else {
                    throw new RuntimeException("error running the monitor thread", e);
                }
            } else {
                logger.error("Error happened", e);
            }
        }
    }

    /**
     * Wait until this crawling session finishes.
     */
    public void waitUntilFinish() {
        while (!finished) {
            synchronized (waitingLock) {
                if (config.isHaltOnError()) {
                    Throwable t = getError();
                    if (t != null && config.isHaltOnError()) {
                        if (t instanceof RuntimeException) {
                            throw (RuntimeException) t;
                        } else if (t instanceof Error) {
                            throw (Error) t;
                        } else {
                            throw new RuntimeException("error on monitor thread", t);
                        }
                    }
                }
                if (finished) {
                    return;
                }
                try {
                    waitingLock.wait();
                } catch (InterruptedException e) {
                    logger.error("Error occurred", e);
                }
            }
        }
    }

    /**
     * Once the crawling session finishes the controller collects the local data of the crawler
     * threads and stores them
     * in a List.
     * This function returns the reference to this list.
     *
     * @return List of Objects which are your local data
     */
    public List<Object> getCrawlersLocalData() {
        return crawlersLocalData;
    }

    protected static void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException ignored) {
            // Do nothing
        }
    }

    /**
     * Adds a new seed URL. A seed URL is a URL that is fetched by the crawler
     * to extract new URLs in it and follow them for crawling.
     *
     * @param pageUrl the URL of the seed
     * @throws InterruptedException
     */
    public void addSeed(String pageUrl) throws InterruptedException {
        addSeed(pageUrl, -1);
    }

    /**
     * Adds a new seed URLs. A seed URL is a URL that is fetched by the crawler
     * to extract new URLs in it and follow them for crawling.
     *
     * @param pageUrls the URL of the seed
     * @throws InterruptedException
     */
    public void addSeeds(List<String> pageUrls) throws InterruptedException {
        List<WebURL> urls = new ArrayList<>();
        for (String pageUrl : pageUrls) {
            WebURL u = addSeedHelper(pageUrl, -1);
            if (u != null) {
                urls.add(u);
            }
        }

        if (!urls.isEmpty()) {
            frontier.scheduleAll(urls);
        }
    }

    /**
     * Adds a new seed URL. A seed URL is a URL that is fetched by the crawler
     * to extract new URLs in it and follow them for crawling. You can also
     * specify a specific document id to be assigned to this seed URL. This
     * document id needs to be unique. Also, note that if you add three seeds
     * with document ids 1,2, and 7. Then the next URL that is found during the
     * crawl will get a doc id of 8. Also you need to ensure to add seeds in
     * increasing order of document ids.
     * <p>
     * Specifying doc ids is mainly useful when you have had a previous crawl
     * and have stored the results and want to start a new crawl with seeds
     * which get the same document ids as the previous crawl.
     *
     * @param pageUrl the URL of the seed
     * @param docId   the document id that you want to be assigned to this seed URL.
     * @throws InterruptedException
     */
    public void addSeed(String pageUrl, int docId) throws InterruptedException {
        WebURL webURL = addSeedHelper(pageUrl, docId);
        if(webURL != null) {
            frontier.schedule(webURL);
        }
    }

    private WebURL addSeedHelper(String pageUrl, int docId) throws InterruptedException {
        String canonicalUrl = normalizer.filter(pageUrl);
        if (canonicalUrl == null) {
            logger.error("Invalid seed URL: {}", pageUrl);
        } else {
            if (docId < 0) {
                docId = docIdServer.getDocId(canonicalUrl);
                if (docId > 0) {
                    logger.trace("This URL is already seen.");
                    return null;
                }
                docId = docIdServer.getNewDocID(canonicalUrl);
            } else {
                try {
                    docIdServer.addUrlAndDocId(canonicalUrl, docId);
                } catch (RuntimeException e) {
                    if (config.isHaltOnError()) {
                        throw e;
                    } else {
                        logger.error("Could not add seed: {}", e.getMessage());
                    }
                }
            }

            WebURL webUrl = webURLFactory.newWebUrl();
            webUrl.setTldList(tldList);
            webUrl.setURL(canonicalUrl);
            webUrl.setDocid(docId);
            webUrl.setDepth((short) 0);
            if (robotstxtServer.allows(webUrl, true)) {
               return webUrl;
            } else {
                // using the WARN level here, as the user specifically asked to add this seed
                logger.warn("Robots.txt does not allow this seed: {}", pageUrl);
            }
        }
        return null;
    }

    /**
     * This function can called to assign a specific document id to a url. This
     * feature is useful when you have had a previous crawl and have stored the
     * Urls and their associated document ids and want to have a new crawl which
     * is aware of the previously seen Urls and won't re-crawl them.
     * <p>
     * Note that if you add three seen Urls with document ids 1,2, and 7. Then
     * the next URL that is found during the crawl will get a doc id of 8. Also
     * you need to ensure to add seen Urls in increasing order of document ids.
     *
     * @param url   the URL of the page
     * @param docId the document id that you want to be assigned to this URL.
     */
    public void addSeenUrl(String url, int docId) {
        String canonicalUrl = normalizer.filter(url);
        if (canonicalUrl == null) {
            logger.error("Invalid Url: {} (can't cannonicalize it!)", url);
        } else {
            try {
                docIdServer.addUrlAndDocId(canonicalUrl, docId);
            } catch (RuntimeException e) {
                if (config.isHaltOnError()) {
                    throw e;
                } else {
                    logger.error("Could not add seen url: {}", e.getMessage());
                }
            }
        }
    }

    public PageFetcher getPageFetcher() {
        return pageFetcher;
    }

    public void setPageFetcher(PageFetcher pageFetcher) {
        this.pageFetcher = pageFetcher;
    }

    public RobotstxtServer getRobotstxtServer() {
        return robotstxtServer;
    }

    public void setRobotstxtServer(RobotstxtServer robotstxtServer) {
        this.robotstxtServer = robotstxtServer;
    }

    public Frontier getFrontier() {
        return frontier;
    }

    public void setFrontier(Frontier frontier) {
        this.frontier = frontier;
    }

    public DocIDServer getDocIdServer() {
        return docIdServer;
    }

    public void setDocIdServer(DocIDServer docIdServer) {
        this.docIdServer = docIdServer;
    }

    public boolean isFinished() {
        return this.finished;
    }

    public boolean isShuttingDown() {
        return shuttingDown;
    }

    /**
     * Set the current crawling session set to 'shutdown'. Crawler threads
     * monitor the shutdown flag and when it is set to true, they will no longer
     * process new pages.
     */
    public void shutdown() {
        logger.info("Shutting down...");
        this.shuttingDown = true;
        pageFetcher.shutDown();
        frontier.finish();
    }

    public CrawlConfig getConfig() {
        return config;
    }

    protected synchronized Throwable getError() {
        return error;
    }

    private synchronized void setError(Throwable e) {
        this.error = e;
    }

    public TLDList getTldList() {
        return tldList;
    }

    public WebURLFactory getWebURLFactory() {
        return webURLFactory;
    }

    public void setWebURLFactory(WebURLFactory webURLFactory) {
        this.webURLFactory = webURLFactory;
    }
}
