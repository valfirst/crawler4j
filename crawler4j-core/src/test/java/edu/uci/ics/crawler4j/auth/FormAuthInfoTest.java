package edu.uci.ics.crawler4j.auth;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;

import crawlercommons.filters.basic.BasicURLNormalizer;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.crawler.authentication.FormAuthInfo;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import edu.uci.ics.crawler4j.test.TestUtils;
import edu.uci.ics.crawler4j.url.SleepycatWebURLFactory;
import edu.uci.ics.crawler4j.url.WebURLFactory;

import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import java.io.File;
import java.util.List;

class FormAuthInfoTest {

    @TempDir
    public File crawlStorageFolder;

    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
        .options(new WireMockConfiguration().dynamicPort())
        .build();

    @Test
    void httpBasicAuth() throws Exception {
        // two pages on first.com behind basic auth
    	wm.stubFor(get(urlEqualTo("/"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/html")
                .withHeader("Host", "localhost")
                .withBody(TestUtils.getInputStringFrom("/html/formAuth/homepage.html")))
                );

    	wm.stubFor(get(urlEqualTo("/login.php"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/html")
                .withHeader("Host", "localhost")
                .withBody(TestUtils.getInputStringFrom("/html/formAuth/login.html")))
              );
    	wm.stubFor(post(urlEqualTo("/login.php"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Set-Cookie", "secret=hash; Path=/")
                .withHeader("Host", "localhost")
        ));
    	wm.stubFor(get(urlEqualTo("/profile.php"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/html")
                .withHeader("Host", "localhost")
                .withHeader("Cookie", "secret=hash")
                .withBody(TestUtils.getInputStringFrom("/html/formAuth/profile.html")))
              );

        CrawlConfig c = new CrawlConfig();
        c.setCrawlStorageFolder(crawlStorageFolder.getAbsolutePath());
        c.setMaxPagesToFetch(10);
        c.setPolitenessDelay(150);
        FormAuthInfo formAuthInfo = new FormAuthInfo(
                "foofy"
                , "superS3cret"
                , "http://localhost:" + wm.getPort() + "/login.php"
                , "username"
                , "password");
        c.setAuthInfos(List.of(formAuthInfo));
        c.setCookieStore(new BasicCookieStore());
        // c.setCookiePolicy(CookiepSpecs.DEFAULT)

        BasicURLNormalizer normalizer = TestUtils.newNormalizer();
				PageFetcher pageFetcher = new PageFetcher(c, normalizer);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        robotstxtConfig.setEnabled(false);
        WebURLFactory webURLFactory = new SleepycatWebURLFactory();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher, webURLFactory);
        CrawlController controller = new CrawlController(c, normalizer, pageFetcher, robotstxtServer, TestUtils.createFrontierConfiguration(c));

        controller.addSeed("http://localhost:" + wm.getPort() + "/");
        controller.start(WebCrawler.class, 1);

        // expect: "POST to credentials"
        wm.verify(exactly(1), postRequestedFor(urlEqualTo("/login.php")));
        wm.verify(exactly(1), getRequestedFor(urlEqualTo("/")));
        wm.verify(exactly(1), getRequestedFor(urlEqualTo("/login.php")));
        wm.verify(exactly(1), getRequestedFor(urlEqualTo("/profile.php"))
            .withCookie("secret", new EqualToPattern("hash"))
        );

        // and: "cookie store is like intended"
        Assertions.assertThat(c.getCookieStore().getCookies()).hasSize(1);
        Assertions.assertThat(c.getCookieStore().getCookies().get(0).getName()).isEqualTo("secret");
        Assertions.assertThat(c.getCookieStore().getCookies().get(0).getValue()).isEqualTo("hash");
    }

}
