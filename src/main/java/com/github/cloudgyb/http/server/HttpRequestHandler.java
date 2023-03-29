package com.github.cloudgyb.http.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpPostRequestDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class HttpRequestHandler extends SimpleChannelInboundHandler<HttpObject> {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private InterfaceHttpPostRequestDecoder postRequestDecoder;
    private HttpRequest currentReq;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        if (msg instanceof HttpRequest req) {
            currentReq = req;
            HttpMethod method = req.method();
            if (method.equals(HttpMethod.POST)) {
                postRequestDecoder = new HttpPostRequestDecoder(req);
            }
            HttpVersion httpVersion = req.protocolVersion();
            logger.info("请求 http 版本 {}， url {}，方法 {}", httpVersion.text(), req.uri(), req.method().name());
            return;
        }
        if (msg instanceof HttpContent httpContent) {
            if (postRequestDecoder != null) { // 如果 post 请求解码器不为 null ，处理 post 请求体
                postRequestDecoder.offer(httpContent);
            } else { // 处理非 post 请求的请求体

            }
        }
        if (msg instanceof LastHttpContent) { // 是否是最后一段消息体
            if (postRequestDecoder != null) {
                List<InterfaceHttpData> bodyHttpDatas = postRequestDecoder.getBodyHttpDatas();
                postRequestDecoder.destroy();
                postRequestDecoder = null;
            } else {

            }
            handleResp(ctx, currentReq.protocolVersion());
            currentReq = null;
        }
    }

    private void handleResp(ChannelHandlerContext ctx, HttpVersion httpVersion) {
        Charset defaultEncoding = StandardCharsets.UTF_8;

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

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        handleExceptionResp(ctx, cause);
        ctx.close();
    }

    private void handleExceptionResp(ChannelHandlerContext ctx, Throwable cause) {
        Charset defaultEncoding = StandardCharsets.UTF_8;
        byte[] respBody = ("<h2 style='text-align:center'>" + cause.getMessage() + "</h2>")
                .getBytes(defaultEncoding);
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.add(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED)
                .add(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_HTML + "; charset=" + defaultEncoding.name());
        DefaultHttpResponse resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, headers);
        ctx.writeAndFlush(resp);
        DefaultHttpContent chunkedContent = new DefaultHttpContent(ctx.alloc().buffer().writeBytes(respBody));
        ctx.writeAndFlush(chunkedContent);
        ctx.writeAndFlush(new DefaultLastHttpContent());
    }
}
