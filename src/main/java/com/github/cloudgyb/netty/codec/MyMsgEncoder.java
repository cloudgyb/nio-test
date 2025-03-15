package com.github.cloudgyb.netty.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class MyMsgEncoder extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        System.out.println("MyMsgEncoder channelRead");
        ctx.fireUserEventTriggered("read event");
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        System.out.println("channelReadComplete");
    }
}
