package com.github.cloudgyb.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.util.ReferenceCountUtil;

public class ByteBufCompositeTest {
    public static void main(String[] args) {
        ByteBuf buffer1 = ByteBufAllocator.DEFAULT.buffer(10);
        ByteBuf buffer2 = ByteBufAllocator.DEFAULT.buffer(10);
        buffer1.writeBytes(new byte[3]);
        buffer2.writeBytes(new byte[3]);
        CompositeByteBuf byteBufs = ByteBufAllocator.DEFAULT.compositeBuffer();
        byteBufs.addComponent(true, buffer1);
        byteBufs.addComponent(true, buffer2);
        System.out.println(byteBufs);
        int i = byteBufs.readableBytes();
        System.out.println(i);
        int i1 = byteBufs.readInt();
        System.out.println(i1);
        ReferenceCountUtil.release(byteBufs);
    }
}
