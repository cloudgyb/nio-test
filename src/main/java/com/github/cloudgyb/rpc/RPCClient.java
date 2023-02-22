package com.github.cloudgyb.rpc;

import com.github.cloudgyb.rpc.protocal.RPCProtocolCodec;
import com.github.cloudgyb.rpc.protocal.RPCResponseMessageHandler;
import com.github.cloudgyb.rpc.service.HelloService;
import com.github.cloudgyb.rpc.service.ServiceProxy;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;

import java.util.Scanner;

/**
 * RPC 客户端
 *
 * @author geng
 * @since 2023/02/22 14:35:10
 */
public class RPCClient {
    public static void main(String[] args) {
        int serverPort = 9090;
        String serverHost = "localhost";
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        try {
            ChannelFuture channelFuture = bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) {
                            ch.pipeline()
                                    .addLast(new LoggingHandler())
                                    .addLast(new RPCProtocolCodec())
                                    .addLast(new RPCResponseMessageHandler());
                        }
                    }).connect(serverHost, serverPort);
            channelFuture.sync();
            Channel channel = channelFuture.channel();
            HelloService helloService = ServiceProxy.newProxy(HelloService.class, channel);
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String aGay = scanner.nextLine();
                if(aGay.equals("q"))
                    break;
                String hello = helloService.sayHello(aGay);
                System.out.println(hello);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }
}
