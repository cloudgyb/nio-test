package com.github.cloudgyb.im.handler;

import com.github.cloudgyb.im.message.ChatRequestMessage;
import com.github.cloudgyb.im.message.ChatResponseMessage;
import com.github.cloudgyb.im.serivce.UserLoginService;
import com.github.cloudgyb.im.session.SessionManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author geng
 * @since 2023/02/18 15:11:49
 */
@ChannelHandler.Sharable
public class ChatRequestMessageHandler extends SimpleChannelInboundHandler<ChatRequestMessage> {
    AtomicReference<String> loginSuccessUsername;
    private final SessionManager sessionManager = SessionManager.DEFAULT;
    private final UserLoginService userLoginService = new UserLoginService();

    public ChatRequestMessageHandler(AtomicReference<String> loginSuccessUsername) {
        this.loginSuccessUsername = loginSuccessUsername;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ChatRequestMessage message) {
        String from = message.getFrom();
        String to = message.getTo();
        String msg = message.getMsg();
        ChatResponseMessage chatResponseMessage = new ChatResponseMessage(from, to, false, "");
        if (loginSuccessUsername != null) { //客户端处理服务器转发过来的消息
            String loginUsername = loginSuccessUsername.get();
            if (loginUsername != null && loginUsername.equals(to)) {
                System.out.printf("[%s]：接收到{%s}发来的消息{%s}!%n", loginUsername, from, msg);
                chatResponseMessage.setSuccess(true);
                chatResponseMessage.setReason("消息发送成功！");
                ctx.channel().writeAndFlush(chatResponseMessage);
                return;
            }
            return;
        }
        //服务转发消息流程
        Channel session = sessionManager.getSession(from);
        if (session == null) {
            chatResponseMessage.setSuccess(false);
            chatResponseMessage.setReason("您未登录！");
            ctx.writeAndFlush(chatResponseMessage);
            return;
        }
        if (!userLoginService.userExist(to)) { //消息接收者是否存在
            chatResponseMessage.setSuccess(false);
            chatResponseMessage.setReason(String.format("消息接收者{%s}不存在！", to));
            ctx.writeAndFlush(chatResponseMessage);
            return;
        }

        Channel toChannel = sessionManager.getSession(to);
        toChannel.writeAndFlush(message);
    }
}
