package com.github.cloudgyb.rpc;

import com.github.cloudgyb.rpc.protocal.RPCProtocolCodec;
import com.github.cloudgyb.rpc.protocal.RPCRequestMessageHandler;
import com.github.cloudgyb.rpc.service.ServiceFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * rpc 服务提供者
 *
 * @author geng
 * @since 2023/02/21 20:34:33
 */
public class RPCServer {
    private final static Logger logger = LoggerFactory.getLogger(RPCServer.class);

    public static void main(String[] args) {
        int port = 9090;
        ServiceFactory.scanService();
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        RPCProtocolCodec rpcServerCodec = new RPCProtocolCodec();
        RPCRequestMessageHandler rpcRequestMessageHandler = new RPCRequestMessageHandler();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        try {
            ChannelFuture channelFuture = serverBootstrap
                    .group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 10)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) {
                            ch.pipeline().addLast(rpcServerCodec)
                                    .addLast(rpcRequestMessageHandler)
                                    .addLast(new LoggingHandler());
                        }
                    }).bind(port);
            channelFuture.sync();
            logger.info("Server start at " + port + "....");
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            worker.shutdownGracefully();
            boss.shutdownGracefully();
        }
    }
}
