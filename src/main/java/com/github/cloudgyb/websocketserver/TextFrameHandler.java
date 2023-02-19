package com.github.cloudgyb.websocketserver;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.nio.charset.StandardCharsets;

/**
 * @author geng
 * @since 2023/02/19 19:39:27
 */
@ChannelHandler.Sharable
public class TextFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        String s = frame.content().toString(StandardCharsets.UTF_8);
        System.out.println(s);
        TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame("哈哈哈");
        ctx.writeAndFlush(textWebSocketFrame);
    }
}
