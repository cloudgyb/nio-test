package com.github.cloudgyb.netty.codec;

import io.netty.channel.embedded.EmbeddedChannel;

public class EncoderTest {
    public static void main(String[] args) {
        EmbeddedChannel embeddedChannel = new EmbeddedChannel();
        embeddedChannel.pipeline().addLast(new MyMsgEncoder()).addLast(new MyMsgEncoder1());
        embeddedChannel.writeInbound("test");
        embeddedChannel.writeInbound("test2");
        embeddedChannel.writeInbound("test3");
        embeddedChannel.close();
    }
}
