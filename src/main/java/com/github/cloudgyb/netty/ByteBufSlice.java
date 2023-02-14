package com.github.cloudgyb.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.util.ReferenceCountUtil;

import java.nio.charset.StandardCharsets;

public class ByteBufSlice {
    public static void main(String[] args) {
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(10);
        buffer.writeCharSequence("123abc", StandardCharsets.UTF_8);

        int i = buffer.readableBytes();
        System.out.println(i);
        int i1 = buffer.writableBytes();
        System.out.println(i1);

        ByteBuf slice = buffer.slice(0, 3);
        System.out.println(slice.toString(StandardCharsets.UTF_8));

        buffer.setByte(0, 'a');

        System.out.println(slice.toString(StandardCharsets.UTF_8));

        ReferenceCountUtil.release(buffer);
    }
}
