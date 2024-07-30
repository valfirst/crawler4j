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
package edu.uci.ics.crawler4j.fetcher;

import java.io.IOException;

import javax.net.ssl.SSLProtocolException;

import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.io.ConnectionEndpoint;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.util.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to work around the exception thrown by the SSL subsystem when the server is incorrectly
 * configured for SNI. In this case, it may return a warning: "handshake alert: unrecognized_name".
 * Browsers usually ignore this warning, while Java SSL throws an exception.
 * <p>
 * This class extends the PoolingHttpClientConnectionManager to catch this exception and retry
 * without
 * the configured hostname, effectively disabling the SNI for this host.
 * <p>
 * Based on the code provided by Ivan Shcheklein, available at:
 * <p>
 * http://stackoverflow.com/questions/7615645/ssl-handshake-alert-unrecognized-name-error-since
 * -upgrade-to-java-1-7-0/28571582#28571582
 */
public class SniPoolingHttpClientConnectionManager extends PoolingHttpClientConnectionManager {
    public static final Logger logger =
            LoggerFactory.getLogger(SniPoolingHttpClientConnectionManager.class);

    public SniPoolingHttpClientConnectionManager(
            Registry<ConnectionSocketFactory> socketFactoryRegistry) {
        super(socketFactoryRegistry);
    }

    public SniPoolingHttpClientConnectionManager(
            Registry<ConnectionSocketFactory> socketFactoryRegistry, DnsResolver dnsResolver) {
        super(socketFactoryRegistry,PoolConcurrencyPolicy.STRICT, PoolReusePolicy.LIFO,
                TimeValue.NEG_ONE_MILLISECOND, null, dnsResolver, null);

    }

    @Override
    public void connect(final ConnectionEndpoint endpoint, final TimeValue connectTimeout, final HttpContext context) throws IOException {
        try {
            super.connect(endpoint, connectTimeout, context);
        } catch (SSLProtocolException e) {
            Boolean enableSniValue =
                    (Boolean) context.getAttribute(SniSSLConnectionSocketFactory.ENABLE_SNI);
            boolean enableSni = enableSniValue == null || enableSniValue;
            if (enableSni && e.getMessage() != null &&
                    e.getMessage().equals("handshake alert:  unrecognized_name")) {
                logger.warn("Server saw wrong SNI host, retrying without SNI");
                context.setAttribute(SniSSLConnectionSocketFactory.ENABLE_SNI, false);
                super.connect(endpoint, connectTimeout, context);
            } else {
                throw e;
            }
        }
    }
}
