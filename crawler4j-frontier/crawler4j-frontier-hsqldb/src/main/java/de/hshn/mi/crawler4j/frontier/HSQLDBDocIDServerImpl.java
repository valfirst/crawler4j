package de.hshn.mi.crawler4j.frontier;

import com.zaxxer.hikari.HikariDataSource;
import de.hshn.mi.crawler4j.exception.HSQLDBFetchException;
import de.hshn.mi.crawler4j.exception.HSQLDBStoreException;
import edu.uci.ics.crawler4j.frontier.DocIDServer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HSQLDBDocIDServerImpl implements DocIDServer {

    private final Object mutex = new Object();

    private HikariDataSource ds;

    public HSQLDBDocIDServerImpl(HikariDataSource ds) {
        this.ds = ds;
    }

    /**
     * Returns the docid of an already seen url.
     *
     * @param url the URL for which the docid is returned.
     * @return the docid of the url if it is seen before. Otherwise -1 is returned.
     */
    @Override
    public int getDocId(String url) {
        synchronized (mutex) {
            return existsWebUrl(url);
        }
    }

    private int existsWebUrl(String url) {
        int docId = -1;
        try (Connection c = ds.getConnection()) {

            try (PreparedStatement ps = c.prepareStatement("SELECT u.id FROM weburl u WHERE u.url = ?")) {
                ps.setString(1, url);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        docId = rs.getInt(1);
                    }
                }
            }

        } catch (SQLException e) {
            throw new HSQLDBFetchException(e);
        }
        return docId;
    }

    @Override
    public int getNewDocID(String url) {
        synchronized (mutex) {
            int docId = existsWebUrl(url);

            if (docId < 0) {
                try (Connection c = ds.getConnection()) {
                    try (PreparedStatement ps = c.prepareStatement("INSERT INTO weburl(id,url) VALUES(nextval('id_master_seq'),?)", new String[]{"id"})) {

                        ps.setString(1, url);
                        ps.executeUpdate();

                        try (ResultSet rs = ps.getGeneratedKeys()) {
                            if (rs.next()) {
                                docId = rs.getInt(1);
                            }
                        }
                    }

                } catch (SQLException e) {
                    throw new HSQLDBStoreException(e);
                }

            }
            return docId;
        }
    }

    @Override
    public void addUrlAndDocId(String url, int docId) {
        synchronized (mutex) {
            try {
                int previousId = getDocId(url);

                if (previousId > 0) {
                    if (previousId == docId) {
                        return;
                    }
                    throw new IllegalArgumentException("Doc id: " + previousId + " is already assigned");
                }

                try (Connection c = ds.getConnection()) {
                    try (PreparedStatement ps = c.prepareStatement("INSERT INTO weburl(id,url) VALUES(?,?)")) {
                        ps.setInt(1, docId);
                        ps.setString(2, url);
                        ps.executeUpdate();
                    }

                } catch (SQLException e) {
                    throw new HSQLDBStoreException(e);
                }

            } catch (RuntimeException e) {
                throw new HSQLDBStoreException(e);
            }
        }
    }

    @Override
    public boolean isSeenBefore(String url) {
        return existsWebUrl(url) > 0;
    }

    @Override
    public int getDocCount() {
        try (Connection c = ds.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("SELECT count(*) FROM weburl")) {
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
    public void close() {
        //nothing to do... ds is closed in frontier configuration...
    }
}
