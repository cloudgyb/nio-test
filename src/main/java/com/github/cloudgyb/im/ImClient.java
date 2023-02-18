package com.github.cloudgyb.im;

import com.github.cloudgyb.im.handler.ChatRequestMessageHandler;
import com.github.cloudgyb.im.handler.ChatResponseMessageHandler;
import com.github.cloudgyb.im.handler.LoginResponseMessageHandler;
import com.github.cloudgyb.im.message.ChatRequestMessage;
import com.github.cloudgyb.im.message.LoginRequestMessage;
import com.github.cloudgyb.im.message.Message;
import com.github.cloudgyb.im.protocol.ImServerCodec;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author geng
 * @since 2023/02/16 21:26:53
 */
public class ImClient {

    private static void menu() {
        System.out.println("-------------菜单--------------------");
        System.out.println("--- login [username] [password] ----");
        System.out.println("--- send [username] [message]   ----");
        System.out.println("--- q                           ----");
        System.out.println("------------------------------------");
    }

    public static void main(String[] args) throws InterruptedException {
        AtomicBoolean isQuit = new AtomicBoolean(false);
        AtomicReference<String> loginSuccessUsername = new AtomicReference<>();
        Scanner scanner = new Scanner(System.in);

        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        LoginResponseMessageHandler loginResponseMessageHandler = new LoginResponseMessageHandler(loginSuccessUsername);
        ChatRequestMessageHandler chatRequestMessageHandler = new ChatRequestMessageHandler(loginSuccessUsername);
        ChatResponseMessageHandler chatResponseMessageHandler = new ChatResponseMessageHandler(loginSuccessUsername);
        ChannelFuture channelFuture = bootstrap.channel(NioSocketChannel.class)
                .group(eventLoopGroup)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel channel) {
                        channel.pipeline().addLast(new ImServerCodec())
                                .addLast(loginResponseMessageHandler)
                                .addLast(chatRequestMessageHandler)
                                .addLast(chatResponseMessageHandler)
                                .addLast(new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws InterruptedException {
                                        channel.close().sync();
                                        isQuit.set(true);
                                        eventLoopGroup.shutdownGracefully();
                                        System.out.println(cause.toString());
                                        System.exit(1);
                                    }
                                });
                    }
                }).connect("localhost", 9090);
        Channel channel = channelFuture.sync().channel();
        while (!isQuit.get()) {
            menu();
            while (!isQuit.get() && scanner.hasNextLine()) {
                String command = scanner.nextLine();
                Message message = null;
                String[] commands = command.split(" ");
                switch (commands[0]) {
                    case "login" -> message = new LoginRequestMessage(commands[1], commands[2]);
                    case "send" -> {
                        if (loginSuccessUsername.get() == null) {
                            System.out.println("用户未登录！");
                            break;
                        }
                        message = new ChatRequestMessage(loginSuccessUsername.get(), commands[1], commands[2]);
                    }
                    case "q" -> isQuit.set(true);
                    default -> {
                    }
                }
                if (message != null) {
                    channel.writeAndFlush(message);
                }
                if (!isQuit.get())
                    menu();
            }
        }
        channel.close().sync();
        eventLoopGroup.shutdownGracefully();
    }
}
