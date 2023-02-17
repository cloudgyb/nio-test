package com.github.cloudgyb.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class LengthFieldDecodeTest {
    public static void main(String[] args) throws IOException {
        EmbeddedChannel embeddedChannel = new EmbeddedChannel();
        embeddedChannel.pipeline()
                .addLast(new LengthFieldBasedFrameDecoder(1024, 0, 4,
                        0, 0))
                .addLast(new LoggingHandler(LogLevel.DEBUG))
                .addLast(new StringDecoder(StandardCharsets.UTF_8))
                .addLast(new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) {
                        System.out.println(msg);
                    }
                });

        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        byte[] msgBody = "Hello world".getBytes(StandardCharsets.UTF_8);

        buffer.writeInt(msgBody.length);
        buffer.writeBytes(msgBody);

        embeddedChannel.writeInbound(buffer);

        embeddedChannel.close();
    }
}
