package com.github.cloudgyb.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.redis.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.ReferenceCountUtil;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;

public class RedisCli1 {
    static class RedisCommandEncode extends ChannelOutboundHandlerAdapter {
        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
            if (msg instanceof String mssg) {
                // *3 $3 set $1 a $1 1
                String[] s = mssg.split(" ");
                ArrayList<RedisMessage> redisMessages = new ArrayList<>();
                //redisMessages.add(new ArrayHeaderRedisMessage(s.length));
                for (String s1 : s) {
                    ByteBuf buffer = ctx.alloc().buffer();
                    buffer.writeBytes(s1.getBytes(StandardCharsets.UTF_8));
                    redisMessages.add(new FullBulkStringRedisMessage(buffer));
                }
                ArrayRedisMessage arrayRedisMessage = new ArrayRedisMessage(redisMessages);
                ctx.writeAndFlush(arrayRedisMessage);
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
                        ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG))
                                .addLast(new RedisEncoder())
                                .addLast(new RedisCommandEncode())
                                .addLast(new RedisDecoder())
                                .addLast(new RedisBulkStringAggregator())
                                .addLast(new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                        System.out.println(msg);
                                        if (msg instanceof FullBulkStringRedisMessage rmsg) {
                                            String s = rmsg.content().toString(StandardCharsets.UTF_8);
                                            System.out.println(s);
                                        }
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
