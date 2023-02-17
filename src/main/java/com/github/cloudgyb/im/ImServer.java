package com.github.cloudgyb.im;

import com.github.cloudgyb.im.message.Message;
import com.github.cloudgyb.im.protocol.ImServerCodec;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;

/**
 * @author geng
 * @since 2023/02/16 21:19:16
 */
public class ImServer {
    private final static Logger logger = LoggerFactory.getLogger(ImServer.class);

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
                                    .addLast(new LoggingHandler(LogLevel.DEBUG))
                                    .addLast(new LengthFieldBasedFrameDecoder(1024, 4, 4,
                                            0, 0))
                                    .addLast(new ImServerCodec())
                                    .addLast(new SimpleChannelInboundHandler<Message>() {
                                        @Override
                                        protected void channelRead0(ChannelHandlerContext ctx, Message message) {
                                            Channel channel = ctx.channel();
                                            SocketAddress remoteAddress = channel.remoteAddress();
                                            logger.info("接收到客户端" + remoteAddress + "消息：" + message);
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
