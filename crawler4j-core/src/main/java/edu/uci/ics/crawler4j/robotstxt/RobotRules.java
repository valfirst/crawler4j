/*-
 * #%L
 * de.hs-heilbronn.mi:crawler4j-core
 * %%
 * Copyright (C) 2010 - 2021 crawler4j-fork
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
package edu.uci.ics.crawler4j.robotstxt;

import crawlercommons.robots.BaseRobotRules;

import java.util.concurrent.TimeUnit;

public class RobotRules {

    // If we fetched it for this host more than
    // 24 hours, we have to re-fetch it.
    private static final long EXPIRATION_DELAY = TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS);

    private final long timeFetched;
    private long timeLastAccessed;

    private final BaseRobotRules wrapped;

    public RobotRules(BaseRobotRules srr) {
        this.wrapped = srr;
        this.timeFetched = System.currentTimeMillis();
    }

    public boolean needsRefetch() {
        return ((System.currentTimeMillis() - timeFetched) > EXPIRATION_DELAY);
    }

    public boolean isAllowed(String url) {
        this.timeLastAccessed = System.currentTimeMillis();
        return wrapped.isAllowed(url);
    }

    public long getTimeLastAccessed() {
        return timeLastAccessed;
    }
}
