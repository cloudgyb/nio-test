package com.github.cloudgyb.protobuf;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author geng
 * @since 2023/03/11 17:21:17
 */
public class Server {
    private final static Logger logger = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) {
        NioEventLoopGroup worker = new NioEventLoopGroup();
        NioEventLoopGroup boss = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        int port = 8888;
        try {
            ChannelFuture channelFuture = serverBootstrap
                    .group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) {
                            ch.pipeline()
                                    .addLast(new ProtobufVarint32FrameDecoder())
                                    .addLast(new ProtobufDecoder(MsgProtocol.Msg.getDefaultInstance()))
                                    .addLast(new SimpleChannelInboundHandler<MsgProtocol.Msg>() {
                                        @Override
                                        protected void channelRead0(ChannelHandlerContext ctx, MsgProtocol.Msg msg) {
                                            String content = msg.getContent();
                                            System.out.println(content);
                                            System.out.println(msg);
                                        }
                                    });
                        }
                    }).bind(port);
            channelFuture.addListener(future -> {
                        if (future.isSuccess()) {
                            logger.info("Server listen at {}....", port);
                        }
                    })
                    .sync()
                    .channel()
                    .closeFuture()
                    .sync();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            worker.shutdownGracefully();
            boss.shutdownGracefully();
        }
    }
}
