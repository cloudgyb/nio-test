package com.github.cloudgyb.http2;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http2.*;

import java.nio.charset.StandardCharsets;

public class Http2RequestHandler extends SimpleChannelInboundHandler<Http2Frame> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Http2Frame msg) {
        if (msg instanceof Http2HeadersFrame http2HeadersFrame &&
                http2HeadersFrame.isEndStream()) {
            String respBody = "Hello world!";
            DefaultHttp2Headers headers = new DefaultHttp2Headers();
            headers.status(HttpResponseStatus.OK.codeAsText());
            headers.add(HttpHeaderNames.CONTENT_LENGTH, respBody.length() + "");
            ctx.write(new DefaultHttp2HeadersFrame(headers)
                    .stream(((Http2HeadersFrame) msg).stream()));
            ByteBuf byteBuf = ctx.alloc().directBuffer();
            byteBuf.writeCharSequence(respBody, StandardCharsets.UTF_8);
            DefaultHttp2DataFrame defaultHttp2DataFrame = new DefaultHttp2DataFrame(
                    byteBuf
            ).stream(((Http2HeadersFrame) msg).stream());
            ctx.write(defaultHttp2DataFrame);
            ctx.writeAndFlush(new DefaultHttp2DataFrame(true).stream(((Http2HeadersFrame) msg).stream()));
        }
    }
}
