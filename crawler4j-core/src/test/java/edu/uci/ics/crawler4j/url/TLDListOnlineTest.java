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
package edu.uci.ics.crawler4j.url;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import java.io.File;
import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

class TLDListOnlineTest {

	@TempDir
  public File crawlStorageFolder;

    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
        .options(new WireMockConfiguration().dynamicPort())
        .build();

    @Test
    void downloadTLDFromUrl() throws IOException {
        // given: "an online TLD file list"
        wm.stubFor(get(urlEqualTo("/tld-names.txt"))
            .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "text/plain")
            .withBody("fakeprovince.ca")
        ));
        // and: "TLDList instance that download fresh list"
        CrawlConfig config = new CrawlConfig();
        config.setOnlineTldListUpdate(true);
        config.setPublicSuffixSourceUrl("http://localhost:" + wm.getPort() + "/tld-names.txt");
        TLDList tldList = new TLDList(config);

        // expect:
        Assertions.assertThat(tldList.contains("fakeprovince.ca")).isTrue();
        Assertions.assertThat(tldList.contains("on.ca")).isFalse();
        wm.verify(1, getRequestedFor(urlEqualTo("/tld-names.txt")));
    }
}
