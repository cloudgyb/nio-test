package com.github.cloudgyb.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;

/**
 * @author geng
 * @since 2023/2/13 15:48
 */
public class EchoServer {
    private final static Logger logger = LoggerFactory.getLogger(EchoServer.class);

    public static void main(String[] args) {
        int port = 9090;
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        try {
            ChannelFuture channelFuture = serverBootstrap
                    .group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 10)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel socketChannel) {
                            socketChannel.pipeline()
                                    .addLast(new StringDecoder())
                                    .addLast(new StringEncoder())
                                    .addLast(new ChannelInboundHandlerAdapter() {
                                        @Override
                                        public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                            Channel channel = ctx.channel();
                                            SocketAddress remoteAddress = channel.remoteAddress();
                                            logger.info("接收到客户端" + remoteAddress + "消息：" + msg);
                                            ctx.channel().writeAndFlush("回显" + msg);
                                        }
                                    });
                        }
                    })
                    .bind(port);
            channelFuture.addListener(future -> System.out.println("Server listen at " + port + "...."));
            channelFuture.channel().closeFuture().sync(); //同步等待通道关闭，避免程序退出。
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            worker.shutdownGracefully();
            boss.shutdownGracefully();
        }
    }
}
