package com.github.cloudgyb.netty;

import io.netty.channel.*;
import io.netty.channel.embedded.EmbeddedChannel;

/**
 * @author geng
 * @since 2023/2/13 16:06
 */
public class PipelineTest {
    public static void main(String[] args) throws InterruptedException {
        EmbeddedChannel channel = new EmbeddedChannel();
        channel.pipeline().addLast(new TestChannelInboundHandlerAdapter());
        channel.pipeline().addLast(new TestChannelInboundHandlerAdapter());
        channel.pipeline().addLast(new TestChannelInboundHandlerAdapter());
        channel.pipeline().addLast(new TestChannelInboundHandlerAdapter());
        channel.pipeline().addLast(new TestChannelInboundHandlerAdapter());
        channel.pipeline().addLast(new TestChannelInboundHandlerAdapter());
        channel.pipeline().addLast(new TestChannelOutboundHandler());
        channel.pipeline().addLast(new TestChannelOutboundHandler());
        channel.pipeline().addLast(new TestChannelOutboundHandler());
        channel.pipeline().addLast(new TestChannelOutboundHandler());
        channel.writeInbound("hello");
        channel.writeOutbound("out hello");
        channel.close().sync();
    }

    static class TestChannelInboundHandlerAdapter extends ChannelInboundHandlerAdapter {
        static int no = 0;
        int n = ++no;

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            System.out.println("channel in handler " + n);
            System.out.println(msg);
            super.channelRead(ctx, msg);
        }
    }

    static class TestChannelOutboundHandler extends ChannelOutboundHandlerAdapter {
        static int no = 0;
        int n = ++no;

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            System.out.println("channel out handler " + n);
            super.write(ctx, msg, promise);
        }
    }
}
