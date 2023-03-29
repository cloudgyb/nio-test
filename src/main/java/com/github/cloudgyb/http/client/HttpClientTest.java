package com.github.cloudgyb.http.client;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.nio.charset.StandardCharsets;

/**
 * @author geng
 * @since 2023/03/26 20:28:19
 */
public class HttpClientTest {
    public static void main(String[] args) throws Exception {
        HttpClientConfig httpClientConfig = new HttpClientConfig("https", "www.baidu.com", 443, "");
        GenericObjectPoolConfig<HttpClient> poolConfig = new GenericObjectPoolConfig<>();
        try (HttpClientPool httpClientPool = new HttpClientPool(httpClientConfig, poolConfig)) {
            HttpClient httpClient = null;
            try {
                httpClient = httpClientPool.borrowObject();
                HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "https://www.baidu.com/");
                FullHttpResponse response = httpClient.send(request);
                ByteBuf content = response.content();
                System.out.println(content.toString(StandardCharsets.UTF_8));
                HttpRequest request1 = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "https://www.baidu.com/index.html");
                FullHttpResponse response1 = httpClient.send(request1);
                String s = response1.content().retain().toString(StandardCharsets.UTF_8);
                System.out.println(s);
            } finally {
                httpClientPool.returnObject(httpClient);
            }
        }
    }
}
