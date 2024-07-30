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
/*
 * Copyright 2018 Paul Galbraith <paul.d.galbraith@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.uci.ics.crawler4j.util;

import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import edu.uci.ics.crawler4j.crawler.*;
import edu.uci.ics.crawler4j.test.SimpleWebURLFactory;
import edu.uci.ics.crawler4j.url.WebURL;

/**
 * Test the Net utility class.
 *   
 * @author Paul Galbraith <paul.d.galbraith@gmail.com>
 */
class NetTest {

    static Net standard;
    static Net allowSingleLevelDomain;
    
    @BeforeAll
    public static void setup() {
    	standard = new Net(new CrawlConfig(), null, new SimpleWebURLFactory());
    	CrawlConfig c = new CrawlConfig();
    	c.setAllowSingleLevelDomain(true);
    	allowSingleLevelDomain = new Net(c, null, new SimpleWebURLFactory());
    }
    		
    @Test
    void noSchemeSpecified() {
        Set<WebURL> extracted = standard.extractUrls("www.wikipedia.com");
        Assertions.assertThat(extracted.stream().map(t -> t.getURL())).contains("http://www.wikipedia.com/");
    }
    
    @Test
    void localhost() {
    	Set<WebURL> extracted = allowSingleLevelDomain.extractUrls("http://localhost/page/1");
    	Assertions.assertThat(extracted.stream().map(t -> t.getURL())).contains("http://localhost/page/1");
    }
    
    @Test
    void noUrlFound() {
    	Set<WebURL> extracted = standard.extractUrls("http://localhost");
      Assertions.assertThat(extracted).isEmpty(); // no expected URL
    }
    
    @Test
    void multipleUrls() {
    	Set<WebURL> extracted = standard.extractUrls(" hey com check out host.com/toodles and http://例子.测试 real soon ");
    	Assertions.assertThat(extracted.stream().map(t -> t.getURL())).contains("http://host.com/toodles", "http://例子.测试/");
    }
    
}
