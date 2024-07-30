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

import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.protocol.HttpContext;

import java.io.IOException;
import java.net.Socket;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

/**
 * Class to work around the exception thrown by the SSL subsystem when the server is incorrectly
 * configured for SNI. In this case, it may return a warning: "handshake alert: unrecognized_name".
 * Browsers usually ignore this warning, while Java SSL throws an exception.
 *
 * This class extends the SSLConnectionSocketFactory to remove the hostname used in the request,
 * which
 * basically disabled SNI for this host.
 *
 * Based on the code provided by Ivan Shcheklein, available at:
 *
 * http://stackoverflow.com/questions/7615645/ssl-handshake-alert-unrecognized-name-error-since
 * -upgrade-to-java-1-7-0/28571582#28571582
 */
public class SniSSLConnectionSocketFactory extends SSLConnectionSocketFactory {
    public static final String ENABLE_SNI = "__enable_sni__";

    /*
     * Implement any constructor you need for your particular application -
     * SSLConnectionSocketFactory has many variants
     */
    public SniSSLConnectionSocketFactory(final SSLContext sslContext,
                                         final HostnameVerifier verifier) {
        super(sslContext, verifier);
    }

    @Override
    public Socket createLayeredSocket(final Socket socket, final String target, final int port,
                                      final HttpContext context) throws IOException {
        Boolean enableSniValue = (Boolean) context.getAttribute(ENABLE_SNI);
        boolean enableSni = enableSniValue == null || enableSniValue;
        return super.createLayeredSocket(socket, enableSni ? target : "", port, context);
    }
}
