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
package edu.uci.ics.crawler4j;

import edu.uci.ics.crawler4j.url.WebURL;

public interface PolitenessServer {


    int NO_POLITENESS_APPLIED = -1;

    /**
     * @param url must not be {@code null}
     * @return the delay in ms or ${@code NO_POLITENESS_APPLIED} (if no politeness needs to be applied)
     */
    long applyPoliteness(WebURL url);

}
