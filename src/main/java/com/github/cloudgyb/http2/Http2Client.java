package com.github.cloudgyb.http2;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpScheme;
import io.netty.handler.codec.http2.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.ssl.*;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import javax.net.ssl.SSLException;

public class Http2Client {
    private final String serverHost;
    private final int serverPort;
    private final SslContext sslContext;

    public Http2Client(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        ApplicationProtocolConfig applicationProtocolConfig = new ApplicationProtocolConfig(ApplicationProtocolConfig.Protocol.ALPN,
                ApplicationProtocolConfig.SelectorFailureBehavior.FATAL_ALERT,
                ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                ApplicationProtocolNames.HTTP_2, ApplicationProtocolNames.HTTP_1_1);
        try {
            sslContext = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .applicationProtocolConfig(applicationProtocolConfig)
                    .sslProvider(SslProvider.JDK)
                    .clientAuth(ClientAuth.NONE)
                    .build();
        } catch (SSLException e) {
            throw new RuntimeException(e);
        }
    }

    public void send() {
        NioEventLoopGroup eventExecutors = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        ChannelFuture connectFuture = bootstrap.channel(NioSocketChannel.class)
                .group(eventExecutors)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("sslHandler", sslContext.newHandler(ch.alloc()))
                                .addLast("alpnHandler", new HttpProtocolNegotiationHandler(false));
                    }
                }).connect(serverHost, serverPort);
        connectFuture.addListener(future -> {
            if (future.isSuccess()) {
                System.out.println("连接已建立！");
            } else {
                System.err.printf("连接建立失败！%s%n", future.cause().getMessage());
                eventExecutors.shutdownGracefully();
            }
        });

    }

}
