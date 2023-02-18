package com.github.cloudgyb.im.handler;

import com.github.cloudgyb.im.message.ChatResponseMessage;
import com.github.cloudgyb.im.session.SessionManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author geng
 * @since 2023/02/18 15:30:51
 */
@ChannelHandler.Sharable
public class ChatResponseMessageHandler extends SimpleChannelInboundHandler<ChatResponseMessage> {
    private final AtomicReference<String> loginSuccessUsername;
    private final SessionManager sessionManager = SessionManager.DEFAULT;

    public ChatResponseMessageHandler(AtomicReference<String> loginSuccessUsername) {
        this.loginSuccessUsername = loginSuccessUsername;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ChatResponseMessage message) {
        String from = message.getFrom();
        String to = message.getTo();
        boolean success = message.isSuccess();
        if (loginSuccessUsername == null) { //服务端处理
            Channel fromChannel = sessionManager.getSession(from);
            fromChannel.writeAndFlush(message);
        } else { //客户端处理
            if (success) {
                System.out.printf("[%s]：发送给{%s} 的消息以送达！%n", from, to);
            } else {
                System.out.printf("[%s]：发送给{%s} 的消息未送达{%s}！%n", from, to, message.getReason());
            }
        }
    }
}
