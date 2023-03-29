package com.github.cloudgyb.http.client;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * @author geng
 * @since 2023/03/25 21:30:57
 */
public class HttpClientPool extends GenericObjectPool<HttpClient> {
    public HttpClientPool(HttpClientConfig httpClientConfig,
                          GenericObjectPoolConfig<HttpClient> poolConfig) {
        super(new HttpClientPooledObjectFactory(httpClientConfig), poolConfig);
        setTestOnBorrow(true);
        setTestOnReturn(true);
    }
}
