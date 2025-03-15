package com.github.cloudgyb.http2;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpScheme;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http2.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

public class HttpProtocolNegotiationHandler extends ApplicationProtocolNegotiationHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final boolean isServer;

    public HttpProtocolNegotiationHandler(boolean isServer) {
        super(ApplicationProtocolNames.HTTP_1_1);
        this.isServer = isServer;
    }

    @Override
    protected void configurePipeline(ChannelHandlerContext ctx, String protocol) throws ExecutionException, InterruptedException {
        logger.debug(ctx.channel() + "选择了{}协议", protocol);
        if (isServer) {
            if (ApplicationProtocolNames.HTTP_1_1.equals(protocol)) {
                ctx.pipeline().addLast("httpServerCodec", new HttpServerCodec());
            } else if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {
                ctx.pipeline().addLast("http2FrameCodec",
                        Http2FrameCodecBuilder.forServer()
                                .frameLogger(new Http2FrameLogger(LogLevel.DEBUG))
                                .build()
                ).addLast(new Http2RequestHandler());
            } else {
                throw new RuntimeException("不支持的协议" + protocol);
            }
        } else {
            if (ApplicationProtocolNames.HTTP_1_1.equals(protocol)) {
                ctx.pipeline().addLast("httpClientCodec", new HttpClientCodec());
            } else if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {
                ctx.pipeline().addLast("http2FrameCodec",
                        Http2FrameCodecBuilder.forClient()
                                .frameLogger(new Http2FrameLogger(LogLevel.DEBUG)).build()
                ).addLast(new Http2MultiplexHandler(new SimpleChannelInboundHandler<Http2Frame>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, Http2Frame msg) {
                    }
                }));
                DefaultHttp2Headers headers = new DefaultHttp2Headers();
                headers.scheme(HttpScheme.HTTPS.name())
                        .method(HttpMethod.GET.asciiName())
                        .path("/");
                DefaultHttp2HeadersFrame http2HeadersFrame = new DefaultHttp2HeadersFrame(headers, true);
                Http2StreamChannelBootstrap http2StreamChannelBootstrap = new Http2StreamChannelBootstrap(ctx.channel());
                Http2StreamChannel http2StreamChannel = http2StreamChannelBootstrap.open().syncUninterruptibly().get();
                http2StreamChannel.pipeline().addLast(new SimpleChannelInboundHandler<Http2Frame>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, Http2Frame msg) {
                        if (msg instanceof Http2HeadersFrame headersFrame) {
                            int id = headersFrame.stream().id();
                            System.out.println("id=" + id + ",headers=" + headersFrame.headers());
                        }
                        if (msg instanceof Http2DataFrame http2DataFrame) {
                            int id = http2DataFrame.stream().id();
                            System.out.println("id=" + id + ",Content=" + http2DataFrame.content().toString(StandardCharsets.UTF_8));
                        }
                    }
                });
                http2StreamChannel.writeAndFlush(http2HeadersFrame);
            } else {
                throw new RuntimeException("不支持的协议" + protocol);
            }
        }
    }

}
