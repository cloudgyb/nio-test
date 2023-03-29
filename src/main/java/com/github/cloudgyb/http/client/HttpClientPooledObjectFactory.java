package com.github.cloudgyb.http.client;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * @author geng
 * @since 2023/03/26 20:08:10
 */
public class HttpClientPooledObjectFactory extends BasePooledObjectFactory<HttpClient> {
    private final HttpClientConfig httpClientConfig;

    public HttpClientPooledObjectFactory(HttpClientConfig httpClientConfig) {
        this.httpClientConfig = httpClientConfig;
    }

    @Override
    public HttpClient create() throws Exception {
        return new HttpClient(this.httpClientConfig.getScheme(), this.httpClientConfig.getHost(),
                this.httpClientConfig.getPort());
    }

    @Override
    public PooledObject<HttpClient> wrap(HttpClient obj) {
        return new DefaultPooledObject<>(obj);
    }

    @Override
    public void destroyObject(PooledObject<HttpClient> p) throws Exception {
        HttpClient httpClient = p.getObject();
        httpClient.close();
    }

    @Override
    public boolean validateObject(PooledObject<HttpClient> p) {
        return !p.getObject().isChannelClosed();
    }
}
