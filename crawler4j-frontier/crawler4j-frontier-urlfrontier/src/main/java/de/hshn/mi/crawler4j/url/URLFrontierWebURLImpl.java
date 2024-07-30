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
package de.hshn.mi.crawler4j.url;

import crawlercommons.urlfrontier.Urlfrontier;
import edu.uci.ics.crawler4j.url.AbstractWebURL;
import edu.uci.ics.crawler4j.url.WebURL;

public class URLFrontierWebURLImpl extends AbstractWebURL implements WebURL {

    private static final Urlfrontier.StringList EMPTY_STRING = Urlfrontier.StringList.newBuilder().addValues("").build();
    private static final Urlfrontier.StringList MINUS_ONE = Urlfrontier.StringList.newBuilder().addValues("-1").build();

    private Urlfrontier.URLInfo rawInfo;
    public URLFrontierWebURLImpl() {

    }

    public URLFrontierWebURLImpl(Urlfrontier.URLInfo info) {
        this.rawInfo = info;
        this.setURL(info.getUrl());
        this.setParentUrl(info.getMetadataOrDefault("parent", EMPTY_STRING).getValues(0));
        this.setParentDocid(Integer.parseInt(info.getMetadataOrDefault("parentdocid", MINUS_ONE).getValues(0)));
        this.setPriority(Byte.parseByte(info.getMetadataOrThrow("priority").getValues(0)));
        this.setDepth(Short.parseShort(info.getMetadataOrThrow("depth").getValues(0)));
        this.setAnchor(info.getMetadataOrDefault("anchor", EMPTY_STRING).getValues(0));
        this.setParentDocid(Integer.parseInt(info.getMetadataOrThrow("docid").getValues(0)));
    }

    public Urlfrontier.URLInfo getRawInfo() {
        return this.rawInfo;
    }
}
