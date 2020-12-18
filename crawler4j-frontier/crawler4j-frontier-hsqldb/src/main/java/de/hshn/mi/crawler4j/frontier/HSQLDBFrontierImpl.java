/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.hshn.mi.crawler4j.frontier;

import com.zaxxer.hikari.HikariDataSource;
import de.hshn.mi.crawler4j.exception.HSQLDBFetchException;
import de.hshn.mi.crawler4j.exception.HSQLDBStoreException;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.frontier.Frontier;
import edu.uci.ics.crawler4j.url.WebURL;
import edu.uci.ics.crawler4j.url.WebURLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class HSQLDBFrontierImpl implements Frontier {

    private static final Logger logger = LoggerFactory.getLogger(HSQLDBFrontierImpl.class);

    private final WebURLFactory factory;
    private final CrawlConfig config;
    private final HikariDataSource ds;

    protected final Object mutex = new Object();
    protected final Object waitingList = new Object();

    protected boolean isFinished = false;
    protected long scheduledPages;

    public HSQLDBFrontierImpl(HikariDataSource ds, WebURLFactory factory, CrawlConfig config) {
        this.ds = ds;
        this.config = config;
        this.factory = factory;
    }

    @Override
    public void scheduleAll(List<WebURL> urls) {
        int maxPagesToFetch = config.getMaxPagesToFetch();
        synchronized (mutex) {
            int newScheduledPage = 0;
            for (WebURL url : urls) {
                if ((maxPagesToFetch > 0) &&
                        ((scheduledPages + newScheduledPage) >= maxPagesToFetch)) {
                    break;
                }

                try {
                    scheduleWebURL(url);
                    newScheduledPage++;
                } catch (HSQLDBStoreException e) {
                    logger.error("Error while putting the url in the work queue", e);
                }

            }
            if (newScheduledPage > 0) {
                scheduledPages += newScheduledPage;
            }
            synchronized (waitingList) {
                waitingList.notifyAll();
            }
        }
    }

    private void scheduleWebURL(WebURL url) {
        try (Connection c = ds.getConnection()) {

            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE weburl " +
                            "SET status = ?, parenturl = ?, parentid = ?, priority = ?, cdepth = ?, anchor = ? WHERE id = ?")) {

                ps.setString(1, Status.SCHEDULED.name());
                ps.setString(2, url.getParentUrl());
                ps.setInt(3, url.getParentDocid());
                ps.setInt(4, url.getPriority());
                ps.setInt(5, url.getDepth());
                ps.setString(6, url.getAnchor());
                ps.setInt(7, url.getDocid());
                ps.executeUpdate();

            }

        } catch (SQLException e) {
            throw new HSQLDBStoreException(e);
        }
    }

    @Override
    public void schedule(WebURL url) {
        int maxPagesToFetch = config.getMaxPagesToFetch();
        synchronized (mutex) {
            try {
                if (maxPagesToFetch < 0 || scheduledPages < maxPagesToFetch) {
                    scheduleWebURL(url);
                    scheduledPages++;
                }
            } catch (HSQLDBStoreException e) {
                logger.error("Error while putting the url in the work queue", e);
            }
        }
    }

    @Override
    public void getNextURLs(int max, List<WebURL> result) {
        while (true) {
            synchronized (mutex) {
                if (isFinished) {
                    return;
                }

                List<WebURL> curResults = new ArrayList<>();

                try (Connection c = ds.getConnection()) {

                    try (PreparedStatement ps = c.prepareStatement("SELECT * FROM weburl u WHERE u.status = ? ORDER BY priority DESC LIMIT ?")) {
                        ps.setString(1, Status.SCHEDULED.name());
                        ps.setInt(2, max);

                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {

                                WebURL webURL = factory.newWebUrl();

                                webURL.setURL(rs.getString("url"));
                                webURL.setDocid(rs.getInt("id"));
                                webURL.setParentDocid(rs.getInt("parentid"));
                                webURL.setParentUrl(rs.getString("parenturl"));
                                webURL.setDepth(rs.getShort("cdepth"));
                                webURL.setPriority(rs.getByte("priority"));
                                webURL.setAnchor(rs.getString("anchor"));

                                curResults.add(webURL);
                            }
                        }
                    }

                } catch (SQLException e) {
                    logger.error(e.getLocalizedMessage(), e);
                }

                result.addAll(curResults);
                for (WebURL curPage : curResults) {
                    try (Connection c = ds.getConnection()) {

                        try (PreparedStatement ps = c.prepareStatement("UPDATE weburl u SET u.status = ? WHERE u.id = ?")) {
                            ps.setString(1, Status.IN_PROCESS.name());
                            ps.setInt(2, curPage.getDocid());
                            ps.executeUpdate();
                        }

                    } catch (SQLException e) {
                        logger.error(e.getLocalizedMessage(), e);
                    }

                }

                if (result.size() > 0) {
                    return;
                }
            }

            try {
                synchronized (waitingList) {
                    waitingList.wait();
                }
            } catch (InterruptedException ignored) {
                // Do nothing
            }
            if (isFinished) {
                return;
            }

        }
    }

    @Override
    public void setProcessed(WebURL webURL) {
        try (Connection c = ds.getConnection()) {

            try (PreparedStatement ps = c.prepareStatement("UPDATE weburl u SET u.status = ? WHERE u.id = ?")) {
                ps.setString(1, Status.COMPLETED.name());
                ps.setInt(2, webURL.getDocid());
                ps.executeUpdate();
            }

        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

    }

    @Override
    public long getQueueLength() {
        return getCount(Status.SCHEDULED);
    }

    private long getCount(Status status) {
        try (Connection c = ds.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("SELECT count(*) FROM weburl WHERE status = ?")) {
                ps.setString(1, status.name());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    } else {
                        return 0;
                    }
                }
            }

        } catch (SQLException e) {
            throw new HSQLDBFetchException(e);
        }
    }

    @Override
    public long getNumberOfAssignedPages() {
        return getCount(Status.IN_PROCESS);
    }

    @Override
    public long getNumberOfProcessedPages() {
        return getCount(Status.COMPLETED);
    }

    @Override
    public long getNumberOfScheduledPages() {
        return getCount(Status.SCHEDULED);
    }

    @Override
    public boolean isFinished() {
        return isFinished;
    }

    @Override
    public void close() {
        //nothing to do
    }

    @Override
    public void finish() {
        isFinished = true;
        synchronized (waitingList) {
            waitingList.notifyAll();
        }
    }
}
