package com.github.cloudgyb.http.client;

import java.net.URI;

/**
 * @author geng
 * @since 2023/03/26 20:11:00
 */
public class HttpClientConfig {
    private final String scheme;
    private final String host;
    private final int port;

    private final String userAgent;

    public HttpClientConfig(String scheme, String host, int port, String userAgent) {
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.userAgent = userAgent;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getScheme() {
        return scheme;
    }

    public URI url() {
        return URI.create(scheme + "://" + host + ":" + port);
    }
}
