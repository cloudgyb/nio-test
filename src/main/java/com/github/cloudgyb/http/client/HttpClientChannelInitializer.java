package com.github.cloudgyb.http.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.Promise;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

/**
 * @author geng
 * @since 2023/03/27 16:12:54
 */
public class HttpClientChannelInitializer extends ChannelInitializer<NioSocketChannel> {
    private final String scheme;
    private volatile Promise<FullHttpResponse> responsePromise;

    public HttpClientChannelInitializer(String scheme) {
        this.scheme = scheme;
    }

    @Override
    protected void initChannel(NioSocketChannel ch) throws Exception {
        if ("https".equals(scheme)) {
            SSLEngine sslEngine = SSLContext.getDefault().createSSLEngine();
            sslEngine.setUseClientMode(true);
            SslHandler sslHandler = new SslHandler(sslEngine, false);
            ch.pipeline().addLast(sslHandler);
        }
        ch.pipeline()
                //.addLast(new LoggingHandler(LogLevel.DEBUG))
                .addLast(new HttpClientCodec())
                .addLast(new HttpObjectAggregator(Integer.MAX_VALUE))
                //.addLast(new LoggingHandler(LogLevel.DEBUG))
                .addLast(new SimpleChannelInboundHandler<FullHttpResponse>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) {
                        msg.retain();
                        HttpClientChannelInitializer.this.responsePromise.setSuccess(msg);
                    }

                    @Override
                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                        ctx.channel().close();
                        HttpClientChannelInitializer.this.responsePromise.setFailure(cause);
                    }
                });
    }

    public void setResponsePromise(Promise<FullHttpResponse> responsePromise) {
        this.responsePromise = responsePromise;
    }
}
