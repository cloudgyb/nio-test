package com.github.cloudgyb.protobuf.custom;

import com.github.cloudgyb.protobuf.MsgProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author geng
 * @since 2023/03/12 11:14:37
 */
public class HeadContentProtoBufCodec extends MessageToMessageCodec<ByteBuf, MsgProtocol.Msg> {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final static short MAGIC_NUM = (short) 0xFEC8;

    @Override
    protected void encode(ChannelHandlerContext ctx, MsgProtocol.Msg msg, List<Object> out) {
        byte[] msgBytes = msg.toByteArray();
        int length = msgBytes.length;
        short version = 0x0001;
        ByteBuf buffer = ctx.alloc().buffer();
        buffer.writeShort(MAGIC_NUM);
        buffer.writeShort(version);
        buffer.writeInt(length);
        buffer.writeBytes(msgBytes);
        out.add(buffer);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        msg.markReaderIndex();
        if (msg.readableBytes() < 8) {
            return;
        }
        short magicNum = msg.readShort();
        if (magicNum != MAGIC_NUM) {
            logger.warn("发现错误的魔数{}，非法数据，关闭连接！", magicNum);
            ctx.close();
            return;
        }
        @SuppressWarnings("unused")
        short version = msg.readShort();
        int msgLength = msg.readInt();
        if (msgLength > msg.readableBytes()) {
            msg.resetReaderIndex();
            return;
        }
        byte[] msgBytes;
        if (msg.hasArray()) {
            ByteBuf slice = msg.slice(msg.readerIndex(), msgLength);
            msgBytes = slice.array();
        } else {
            msgBytes = new byte[msgLength];
            msg.readBytes(msgBytes, 0, msgLength);
        }
        MsgProtocol.Msg msg1 = MsgProtocol.Msg.parseFrom(msgBytes);
        if (msg1 != null) {
            out.add(msg1);
        }
    }

    public static void main(String[] args) {
        ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        buffer.writeBytes(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});

        buffer.markReaderIndex();
        byte b = buffer.readByte();
        System.out.println(b);

        System.out.println(buffer.readerIndex());

        buffer.resetReaderIndex();

        System.out.println(buffer.readerIndex());
    }
}
