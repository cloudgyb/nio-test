package com.github.cloudgyb.im.protocol;

import com.github.cloudgyb.im.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * @author geng
 * @since 2023/02/16 20:52:25
 */
public class ImServerCodec extends MessageToMessageCodec<ByteBuf, Message> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected void encode(ChannelHandlerContext ctx, Message message, List<Object> list) throws Exception {
        ByteBuf buffer = ctx.alloc().buffer();
        // 1. 消息魔术 2 字节
        buffer.writeShort(Message.magicNum);
        // 2. 消息版本 1 字节
        buffer.writeByte(1);
        // 3. 消息编码方式 1 字节
        buffer.writeByte(Message.serial_jdk);

        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(arrayOutputStream);
        objectOutputStream.writeObject(message);
        byte[] bytes = arrayOutputStream.toByteArray();
        objectOutputStream.close();
        arrayOutputStream.close();
        // 4. 消息长度 4 字节
        buffer.writeInt(bytes.length);
        // 5. 消息体
        buffer.writeBytes(bytes);

        list.add(buffer);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) throws Exception {
        int magicNum = byteBuf.readShort();
        if (magicNum != Message.magicNum) {
            ctx.channel().close();
        } else {
            byte version = byteBuf.readByte();
            byte serialType = byteBuf.readByte();
            int msgLength = byteBuf.readInt();
            logger.info("magicNum:{},version:{},serialType:{},msgLength:{}", magicNum, version, serialType, msgLength);
            byte[] msg = new byte[msgLength];
            byteBuf.readBytes(msg);

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(msg);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            Object o = objectInputStream.readObject();
            list.add(o);
        }
    }
}
