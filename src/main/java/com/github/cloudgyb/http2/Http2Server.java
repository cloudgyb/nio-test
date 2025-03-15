package com.github.cloudgyb.http2;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.compression.ZstdOptions;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http2.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.ssl.*;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.util.concurrent.Future;

import javax.net.ssl.SSLException;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.util.concurrent.atomic.AtomicBoolean;

public class Http2Server {
    private final String host;
    private final int port;
    private final NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private final NioEventLoopGroup workerGroup = new NioEventLoopGroup();
    private Channel serverChannel;
    private final SslContext sslContext;

    public Http2Server(String host, int port) {
        this.host = host;
        this.port = port;
        SelfSignedCertificate selfSignedCertificate;
        try {
            selfSignedCertificate = new SelfSignedCertificate();

        } catch (CertificateException e) {
            throw new RuntimeException(e);
        }
        ApplicationProtocolConfig applicationProtocolConfig = new ApplicationProtocolConfig(ApplicationProtocolConfig.Protocol.ALPN,
                ApplicationProtocolConfig.SelectorFailureBehavior.FATAL_ALERT,
                ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                ApplicationProtocolNames.HTTP_2, ApplicationProtocolNames.HTTP_1_1);
        try {

            sslContext = SslContextBuilder.forServer(selfSignedCertificate.key(), selfSignedCertificate.cert())
                    .applicationProtocolConfig(applicationProtocolConfig)
                    .sslProvider(SslProvider.JDK)
                    .clientAuth(ClientAuth.NONE)
                    .build();
        } catch (SSLException e) {
            throw new RuntimeException(e);
        }
    }

    public void start() {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        ChannelFuture channelFuture = serverBootstrap.group(bossGroup, workerGroup)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) {
                        ch.pipeline()
                                .addLast(sslContext.newHandler(ch.alloc()))
                                .addLast(new HttpProtocolNegotiationHandler(true));
                    }
                }).bind(host, port);
        serverChannel = channelFuture.channel();
        channelFuture.addListener(future -> {
            if (future.isSuccess()) {
                System.out.printf("Http2 Server Started：%s:%d%n", host, port);
            } else {
                System.err.printf("Http2 Server Started failed!：%s:%d %s %n", host, port, future.cause().getMessage());
            }
        });
    }

    public void stop() {
        if (serverChannel != null) {
            serverChannel.close();
        }
        workerGroup.shutdownGracefully().addListener(future -> {
            if (future.isSuccess()) {
                System.out.println("Http2 Server workerGroup Shutdown");
            } else {
                System.out.printf("Http2 Server workerGroup Shutdown failed:%s%n", future.cause().getMessage());
            }
        });
        bossGroup.shutdownGracefully().addListener(future -> {
            if (future.isSuccess()) {
                System.out.println("Http2 Server bossGroup Shutdown");
            } else {
                System.out.printf("Http2 Server bossGroup Shutdown failed:%s%n", future.cause().getMessage());
            }
        });
    }
}
