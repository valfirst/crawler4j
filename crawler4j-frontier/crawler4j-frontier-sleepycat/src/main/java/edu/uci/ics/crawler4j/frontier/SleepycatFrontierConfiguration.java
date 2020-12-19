package edu.uci.ics.crawler4j.frontier;

import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.url.SleepycatWebURLFactory;
import edu.uci.ics.crawler4j.url.WebURLFactory;
import edu.uci.ics.crawler4j.util.IO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

public class SleepycatFrontierConfiguration implements FrontierConfiguration {

    protected static final Logger logger = LoggerFactory.getLogger(SleepycatFrontierImpl.class);

    private final SleepycatFrontierImpl frontier;
    private final SleepycatDocIDServer docIdServer;
    private final Environment env;

    public SleepycatFrontierConfiguration(CrawlConfig config) throws Exception {
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setAllowCreate(true);
        envConfig.setTransactional(config.isResumableCrawling());
        envConfig.setLocking(config.isResumableCrawling());
        envConfig.setLockTimeout(config.getDbLockTimeout(), TimeUnit.MILLISECONDS);

        File envHome = new File(config.getCrawlStorageFolder() + File.separator + "frontier");
        if (!envHome.exists()) {
            try {
                Files.createDirectory(envHome.toPath());
                logger.debug("Created folder: " + envHome.getAbsolutePath());
            } catch (IOException e) {
                logger.error(e.getLocalizedMessage(), e);
                throw new Exception(
                        "Failed creating the frontier folder: " + envHome.getAbsolutePath());

            }
        }

        if (!config.isResumableCrawling()) {
            IO.deleteFolderContents(envHome);
            logger.info("Deleted contents of: " + envHome +
                    " ( as you have configured resumable crawling to false )");
        }

        this.env = new Environment(envHome, envConfig);
        this.docIdServer = new SleepycatDocIDServer(env, config);
        this.frontier = new SleepycatFrontierImpl(env, config);
    }

    @Override
    public DocIDServer getDocIDServer() {
        return docIdServer;
    }

    @Override
    public Frontier getFrontier() {
        return frontier;
    }

    @Override
    public WebURLFactory getWebURLFactory() {
        return new SleepycatWebURLFactory();
    }

    @Override
    public void close() {
        env.close();
    }
}
