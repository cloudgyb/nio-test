package com.github.cloudgyb.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.ReferenceCountUtil;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class RedisCli {
    static class RedisCommandEncode extends ChannelOutboundHandlerAdapter {
        private final static byte[] CRLF = {13, 10};

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
            if (msg instanceof String mssg) {
                if (mssg.length() == 0)
                    return;
                String[] s = mssg.split(" ");
                ByteBuf buffer = ctx.alloc().buffer();
                buffer.writeBytes(("*" + s.length).getBytes(StandardCharsets.UTF_8));
                buffer.writeBytes(CRLF);
                for (String s1 : s) {
                    buffer.writeBytes(("$" + s1.length()).getBytes(StandardCharsets.UTF_8));
                    buffer.writeBytes(CRLF);
                    buffer.writeBytes(s1.getBytes(StandardCharsets.UTF_8))
                            .writeBytes(CRLF);
                }
                ctx.writeAndFlush(buffer);
            } else {
                ReferenceCountUtil.release(msg);
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup eventExecutors = new NioEventLoopGroup();
        ChannelFuture channelFuture = bootstrap.channel(NioSocketChannel.class)
                .group(eventExecutors)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) {
                        ch.pipeline().addLast(new RedisCommandEncode());
                        ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                        ch.pipeline().addLast(new StringDecoder())
                                .addLast(new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                        System.out.println(msg);
                                    }
                                });
                    }
                })
                .connect("localhost", 6379)
                .sync();

        Channel channel = channelFuture.channel();
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();
            if (command.equals("q"))
                break;
            channel.writeAndFlush(command);
        }
        eventExecutors.shutdownGracefully();
        channel.close();
    }
}
