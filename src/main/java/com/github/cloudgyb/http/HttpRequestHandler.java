package com.github.cloudgyb.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class HttpRequestHandler extends SimpleChannelInboundHandler<HttpRequest> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpRequest req) {
        logger.info(req.getClass().getName());
        HttpVersion httpVersion = req.protocolVersion();
        Charset defaultEncoding = StandardCharsets.UTF_8;
        logger.info("请求 http 版本 {}， url {}，方法 {}", httpVersion.text(), req.uri(), req.method().name());
        byte[] respBody = "<h2 style='text-align:center'>Welcome to Netty Http Server! 欢迎使用 Netty Http Server ！</h2>"
                .getBytes(defaultEncoding);
        // 1. body 长度响应时已知，使用 CONTENT-LENGTH 指定 body 的长度
        /*DefaultFullHttpResponse resp = new DefaultFullHttpResponse(httpVersion, HttpResponseStatus.OK);
        resp.headers()
                .add(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_HTML + "; charset=" + defaultEncoding.name())
                .add(HttpHeaderNames.CONTENT_LENGTH, respBody.length);
        resp.content().writeBytes(respBody);
        ctx.writeAndFlush(resp);*/
        // 2. 假设 body 是动态生成的，事先不知道长度可以使用 chunked 编码
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.add(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED)
                .add(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_HTML + "; charset=" + defaultEncoding.name());
        DefaultHttpResponse resp = new DefaultHttpResponse(httpVersion, HttpResponseStatus.OK, headers);
        ctx.writeAndFlush(resp);
        DefaultHttpContent chunkedContent = new DefaultHttpContent(ctx.alloc().buffer().writeBytes(respBody));
        ctx.writeAndFlush(chunkedContent);
        ctx.writeAndFlush(new DefaultLastHttpContent());
    }
}
