package com.github.cloudgyb.im.handler;

import com.github.cloudgyb.im.message.LoginRequestMessage;
import com.github.cloudgyb.im.message.LoginResponseMessage;
import com.github.cloudgyb.im.serivce.UserLoginService;
import com.github.cloudgyb.im.session.SessionManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;

/**
 * @author geng
 * @since 2023/02/17 21:33:38
 */
@ChannelHandler.Sharable
public class LoginRequestMessageHandler extends SimpleChannelInboundHandler<LoginRequestMessage> {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final UserLoginService userLoginService = new UserLoginService();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoginRequestMessage loginRequestMessage) throws Exception {
        Channel channel = ctx.channel();
        SocketAddress remoteAddress = channel.remoteAddress();
        logger.info("接收到客户端" + remoteAddress + "登录消息：" + loginRequestMessage);
        String username = loginRequestMessage.getUsername();
        String password = loginRequestMessage.getPassword();
        LoginResponseMessage loginResponseMessage = new LoginResponseMessage();
        loginResponseMessage.setUsername(username);
        if (StringUtil.isNullOrEmpty(username) || StringUtil.isNullOrEmpty(password)) {
            loginResponseMessage.setSuccess(false);
            loginResponseMessage.setReason("用户名和密码不能为空！");
            ctx.writeAndFlush(loginResponseMessage);
            return;
        }
        boolean isLoginSuccess = userLoginService.login(username, password);
        if (isLoginSuccess) {
            loginResponseMessage.setSuccess(true);
            loginResponseMessage.setReason("登录成功！");
            SessionManager.DEFAULT.saveSession(username, ctx.channel());
        } else {
            loginResponseMessage.setSuccess(false);
            loginResponseMessage.setReason("用户名或密码错误！");
        }
        logger.info(loginResponseMessage.toString());
        ctx.writeAndFlush(loginResponseMessage);
    }
}
