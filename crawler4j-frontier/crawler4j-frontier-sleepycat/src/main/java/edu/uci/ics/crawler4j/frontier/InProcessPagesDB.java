package edu.uci.ics.crawler4j.frontier;

import edu.uci.ics.crawler4j.url.WebURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;

import edu.uci.ics.crawler4j.url.WebURLImpl;

/**
 * This class maintains the list of pages which are
 * assigned to crawlers but are not yet processed.
 * It is used for resuming a previous crawl.
 *
 * @author Yasser Ganjisaffar
 */
public class InProcessPagesDB extends WorkQueues {
    private static final Logger logger = LoggerFactory.getLogger(InProcessPagesDB.class);

    private static final String DATABASE_NAME = "InProcessPagesDB";

    public InProcessPagesDB(Environment env) {
        super(env, DATABASE_NAME, true);
        long docCount = getLength();
        if (docCount > 0) {
            logger.info("Loaded {} URLs that have been in process in the previous crawl.",
                        docCount);
        }
    }

    public boolean removeURL(WebURL webUrl) {
        synchronized (mutex) {
            DatabaseEntry key = getDatabaseEntryKey(webUrl);
            DatabaseEntry value = new DatabaseEntry();
            Transaction txn = beginTransaction();
            try (Cursor cursor = openCursor(txn)) {
                OperationStatus result = cursor.getSearchKey(key, value, null);

                if (result == OperationStatus.SUCCESS) {
                    result = cursor.delete();
                    if (result == OperationStatus.SUCCESS) {
                        return true;
                    }
                }
            } finally {
                commit(txn);
            }
        }
        return false;
    }
}