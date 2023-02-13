package com.github.cloudgyb.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author geng
 * @since 2023/2/13 14:25
 */
public class ByteBufTest {
    public static void main(String[] args) {
        ByteBuf buffer = Unpooled.buffer(10);
        buffer.writeByte(1);
        buffer.writeInt(12);
        int anInt = buffer.getInt(0);
        System.out.println(anInt);
        ByteBuf byteBuf = Unpooled.directBuffer(10);
        System.out.println(byteBuf.isReadable());
    }
}
