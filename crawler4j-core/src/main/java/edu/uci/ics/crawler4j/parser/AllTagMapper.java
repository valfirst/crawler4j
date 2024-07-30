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
package edu.uci.ics.crawler4j.parser;

import org.apache.tika.parser.html.HtmlMapper;

import java.util.Locale;

/**
 * Maps all HTML tags (not ignore some of this)
 *
 * @author Andrey Nikolaev (vajadhava@gmail.com)
 */
public class AllTagMapper implements HtmlMapper {

    @Override
    public String mapSafeElement(String name) {
        return name.toLowerCase(Locale.ROOT);
    }

    @Override
    public boolean isDiscardElement(String name) {
        return false;
    }

    @Override
    public String mapSafeAttribute(String elementName, String attributeName) {
        return attributeName.toLowerCase(Locale.ROOT);
    }
}
