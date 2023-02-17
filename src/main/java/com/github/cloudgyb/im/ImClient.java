package com.github.cloudgyb.im;

import com.github.cloudgyb.im.message.LoginRequestMessage;
import com.github.cloudgyb.im.protocol.ImServerCodec;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author geng
 * @since 2023/02/16 21:26:53
 */
public class ImClient {
    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        ChannelFuture channelFuture = bootstrap.channel(NioSocketChannel.class)
                .group(eventLoopGroup)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel channel) {
                        channel.pipeline().addLast(new ImServerCodec())
                                .addLast(new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                        System.out.println(msg);
                                    }
                                });
                    }
                }).connect("localhost", 9090);
        Channel channel = channelFuture.sync().channel();
        LoginRequestMessage loginRequestMessage = new LoginRequestMessage(1);
        loginRequestMessage.setUsername("geng");
        loginRequestMessage.setPassword("123456");
        channel.writeAndFlush(loginRequestMessage);
        channel.close().sync();
        eventLoopGroup.shutdownGracefully();
    }
}
