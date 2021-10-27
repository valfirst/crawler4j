/*-
 * #%L
 * de.hs-heilbronn.mi:crawler4j-commons
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
package edu.uci.ics.crawler4j.url;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import crawlercommons.domains.EffectiveTldFinder;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;

/**
 * This class obtains a list of eTLDs (from online or a local file) in order to
 * determine private/public components of domain names per definition at
 * <a href="https://publicsuffix.org">publicsuffix.org</a>.
 */
public class TLDList {

    public TLDList(CrawlConfig config) throws IOException {
        if (config.isOnlineTldListUpdate()) {
            InputStream stream;
            String filename = config.getPublicSuffixLocalFile();
            if (filename == null) {
                URL url = new URL(config.getPublicSuffixSourceUrl());
                stream = url.openStream();
            } else {
                stream = new FileInputStream(filename);
            }
            try {
                EffectiveTldFinder.getInstance().initialize(stream);
            } finally {
                stream.close();
            }
        }
    }

    public boolean contains(String domain) {
        return EffectiveTldFinder.getAssignedDomain(domain) != null;
    }

    public boolean isRegisteredDomain(String domain) {
        return EffectiveTldFinder.getEffectiveTLD(domain) != null;
    }
}
