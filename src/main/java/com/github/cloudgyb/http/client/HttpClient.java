package com.github.cloudgyb.http.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * http 客户端
 *
 * @author geng
 * @since 2023/03/24 19:28:12
 */
public class HttpClient implements Closeable {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final NioEventLoopGroup eventLoopGroup;
    private final Channel channel;
    private final String scheme;
    private final String host;
    private final int port;
    private final HttpClientChannelInitializer httpClientChannelInitializer;

    public HttpClient(String scheme, String host, int port) throws Exception {
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.eventLoopGroup = new NioEventLoopGroup(1);
        Bootstrap bootstrap = new Bootstrap();
        this.httpClientChannelInitializer = new HttpClientChannelInitializer(this.scheme);
        try {
            bootstrap.group(this.eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(httpClientChannelInitializer);
            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            this.channel = channelFuture.channel();
            logger.info("{}:{}连接已建立！", host, port);
        } catch (Exception e) {
            logger.error("{}:{}连接建立失败！", host, port);
            throw e;
        }
    }

    public Promise<FullHttpResponse> sendAsync(HttpRequest request) {
        DefaultPromise<FullHttpResponse> promise = new DefaultPromise<>(GlobalEventExecutor.INSTANCE);
        this.httpClientChannelInitializer.setResponsePromise(promise);
        this.channel.writeAndFlush(request);
        return promise;
    }


    public FullHttpResponse send(HttpRequest request) throws Exception {
        Promise<FullHttpResponse> fullHttpResponsePromise = sendAsync(request);
        return fullHttpResponsePromise.get(10, TimeUnit.SECONDS);
    }

    @Override
    public void close() throws IOException {
        logger.info("开始关闭连接{}：{}", host, port);
        this.channel.close();
        this.eventLoopGroup.shutdownGracefully();
        logger.info("连接{}：{}已关闭！", host, port);
    }

    public boolean isChannelClosed() {
        return !this.channel.isActive();
    }
}
