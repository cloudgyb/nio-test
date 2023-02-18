package com.github.cloudgyb.im.handler;

import com.github.cloudgyb.im.message.LoginResponseMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author geng
 * @since 2023/02/18 11:19:08
 */
@ChannelHandler.Sharable
public class LoginResponseMessageHandler extends SimpleChannelInboundHandler<LoginResponseMessage> {
    private final AtomicReference<String> loginSuccessUsername;

    public LoginResponseMessageHandler(AtomicReference<String> loginSuccessUsername) {
        this.loginSuccessUsername = loginSuccessUsername;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoginResponseMessage message) {
        boolean success = message.isSuccess();
        String username = message.getUsername();
        String reason = message.getReason();
        if (success) {
            loginSuccessUsername.set(username);
            System.out.printf("{%s}登录成功！%n", username);
        } else {
            System.out.printf("{%s}登录失败！{%s}%n", username, reason);
        }
    }
}
