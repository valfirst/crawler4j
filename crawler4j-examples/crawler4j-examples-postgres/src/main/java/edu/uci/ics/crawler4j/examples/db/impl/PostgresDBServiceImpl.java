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
package edu.uci.ics.crawler4j.examples.db.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.slf4j.Logger;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.examples.db.PostgresDBService;
import edu.uci.ics.crawler4j.parser.HtmlParseData;

public class PostgresDBServiceImpl implements PostgresDBService {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(PostgresDBServiceImpl.class);

    private final ComboPooledDataSource comboPooledDataSource;

    private final PreparedStatement insertKeyStatement;

    public PostgresDBServiceImpl(ComboPooledDataSource comboPooledDataSource) throws SQLException {
        this.comboPooledDataSource = comboPooledDataSource;
        insertKeyStatement = comboPooledDataSource.getConnection().prepareStatement("insert into webpage values " +
                "(nextval('id_master_seq'),?,?,?,?)");
    }

    @Override
    public void store(Page page) {

        if (page.getParseData() instanceof HtmlParseData) {
            try {

                HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();

                insertKeyStatement.setString(1, htmlParseData.getHtml());
                insertKeyStatement.setString(2, htmlParseData.getText());
                insertKeyStatement.setString(3, page.getWebURL().getURL());
                insertKeyStatement.setTimestamp(4, new Timestamp(new java.util.Date().getTime()));
                insertKeyStatement.executeUpdate();
            } catch (SQLException e) {
                logger.error("SQL Exception while storing webpage for url'{}'", page.getWebURL().getURL(), e);
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void close() {
        if (comboPooledDataSource != null) {
            comboPooledDataSource.close();
        }
    }
}
