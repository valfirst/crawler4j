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
package edu.uci.ics.crawler4j.frontier;

import edu.uci.ics.crawler4j.url.WebURL;

import java.util.List;

public interface Frontier {

    void scheduleAll(List<WebURL> urls);

    void schedule(WebURL url);

    void getNextURLs(int max, List<WebURL> result);

    void setProcessed(WebURL webURL);

    long getQueueLength();

    long getNumberOfAssignedPages();

    long getNumberOfProcessedPages();

    long getNumberOfScheduledPages();

    boolean isFinished();

    void close();

    void finish();

}
