package com.github.cloudgyb.protobuf.custom;

import com.github.cloudgyb.protobuf.MsgProtocol;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

/**
 * @author geng
 * @since 2023/03/11 17:45:19
 */
public class Client {
    private final static Logger logger = LoggerFactory.getLogger(Client.class);

    public static void main(String[] args) {
        int serverPort = 8888;
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        try {
            ChannelFuture channelFuture = bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) {
                            ch.pipeline()
                                    .addLast(new HeadContentProtoBufCodec());
                        }
                    })
                    .connect("localhost", serverPort);
            channelFuture.sync();
            logger.info("已连接到 server (localhost:{})", serverPort);
            Channel channel = channelFuture.channel();
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String msgContent = scanner.nextLine();
                if ("q".equals(msgContent)) {
                    break;
                }
                MsgProtocol.Msg msg = MsgProtocol.Msg.newBuilder().setId(1000)
                        .setContent(msgContent).build();
                channel.writeAndFlush(msg);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }
}
