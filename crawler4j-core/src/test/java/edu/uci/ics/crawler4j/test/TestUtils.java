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
package edu.uci.ics.crawler4j.test;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import crawlercommons.filters.basic.BasicURLNormalizer;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.frontier.FrontierConfiguration;
import edu.uci.ics.crawler4j.frontier.SleepycatFrontierConfiguration;

public final class TestUtils {
	private TestUtils() {
	}
	
	
	public static String getInputStringFrom(final String resourcePath) {
		try {
			return Files.readString(//
					new File(TestUtils.class.getResource(resourcePath).toURI()).toPath()//
					, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static BasicURLNormalizer newNormalizer() {
		return BasicURLNormalizer.newBuilder()//
				.idnNormalization(BasicURLNormalizer.IdnNormalization.NONE)//
				.build();
	}
	
	public static FrontierConfiguration createFrontierConfiguration(final CrawlConfig config) {
		try {
			return new SleepycatFrontierConfiguration(config, 10);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}
}
