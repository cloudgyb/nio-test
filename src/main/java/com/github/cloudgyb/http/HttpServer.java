package com.github.cloudgyb.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.multipart.HttpPostMultipartRequestDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Http Server
 */
public class HttpServer {
    private final static Logger logger = LoggerFactory.getLogger(HttpServer.class);

    public static void main(String[] args) {
        String host = "0.0.0.0";
        int port = 90;
        InetSocketAddress bindAddress = new InetSocketAddress(host, port);
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        try {
            ChannelFuture channelFuture = serverBootstrap.channel(NioServerSocketChannel.class)
                    .group(boss, worker)
                    .option(ChannelOption.SO_BACKLOG, 10)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) {
                            ch.pipeline()
                                    .addLast(new HttpServerCodec())
                                    .addLast(new HttpObjectAggregator(2048))
                                    .addLast(new HttpRequestHandler())
                                    .addLast(new ChannelInboundHandlerAdapter() {
                                        @Override
                                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                            System.out.println(msg);
                                        }
                                    });
                        }
                    }).bind(bindAddress);
            channelFuture.addListener(future -> {
                boolean success = future.isSuccess();
                if (success) {
                    logger.info("Http Server listen at " + bindAddress);
                } else {
                    logger.error("Http Server start failed!", channelFuture.cause());
                }
            });
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            worker.shutdownGracefully();
            boss.shutdownGracefully();
        }
    }
}
