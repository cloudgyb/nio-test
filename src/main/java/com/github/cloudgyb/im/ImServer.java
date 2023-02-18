package com.github.cloudgyb.im;

import com.github.cloudgyb.im.handler.ChatRequestMessageHandler;
import com.github.cloudgyb.im.handler.ChatResponseMessageHandler;
import com.github.cloudgyb.im.handler.LoginRequestMessageHandler;
import com.github.cloudgyb.im.protocol.ImServerCodec;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * @author geng
 * @since 2023/02/16 21:19:16
 */
public class ImServer {

    public static void main(String[] args) {
        int port = 9090;
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        try {
            LoginRequestMessageHandler loginRequestMessageHandler = new LoginRequestMessageHandler();
            ChatRequestMessageHandler chatRequestMessageHandler = new ChatRequestMessageHandler(null);
            ChatResponseMessageHandler chatResponseMessageHandler = new ChatResponseMessageHandler(null);
            ChannelFuture channelFuture = serverBootstrap
                    .group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 10)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel socketChannel) {
                            socketChannel.pipeline()
                                    //.addLast(new LoggingHandler(LogLevel.DEBUG))
                                    .addLast(new LengthFieldBasedFrameDecoder(1024, 4, 4,
                                            0, 0))
                                    .addLast(new ImServerCodec())
                                    .addLast(loginRequestMessageHandler)
                                    .addLast(chatRequestMessageHandler)
                                    .addLast(chatResponseMessageHandler);
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
