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