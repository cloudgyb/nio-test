package com.github.cloudgyb.websocketserver;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author geng
 * @since 2023/2/13 15:48
 */
public class WebsocketServer {
    private final static Logger logger = LoggerFactory.getLogger(com.github.cloudgyb.netty.EchoServer.class);

    public static void main(String[] args) {
        int port = 9090;
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        TextFrameHandler textFrameHandler = new TextFrameHandler();
        try {
            ChannelFuture channelFuture = serverBootstrap
                    .group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 10)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel socketChannel) {
                            socketChannel.pipeline()
                                    .addLast(new LoggingHandler(LogLevel.DEBUG))
                                    .addLast(new HttpRequestDecoder())
                                    .addLast(new HttpResponseEncoder())
                                    .addLast(new HttpObjectAggregator(2048, true))
                                    .addLast(new WebSocketServerProtocolHandler("/chat", "chat",
                                            true, 2028, 2000) {
                                        @Override
                                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
                                            System.out.println(evt);
                                        }
                                    })
                                    .addLast(textFrameHandler);
                        }
                    })
                    .bind(port);
            channelFuture.addListener(future -> logger.info("Server listen at " + port + "...."));
            channelFuture.channel().closeFuture().sync(); //同步等待通道关闭，避免程序退出。
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            worker.shutdownGracefully();
            boss.shutdownGracefully();
        }
    }
}
