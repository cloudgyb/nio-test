package com.github.cloudgyb.rpc;

import com.github.cloudgyb.rpc.message.MessageSerialTypeEnum;
import com.github.cloudgyb.rpc.message.RPCMessage;
import com.github.cloudgyb.rpc.message.RPCRequestMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.List;

/**
 * rpc 请求编解码器
 *
 * @author geng
 * @since 2023/02/21 20:52:35
 */
public class RPCRequestMessageCodec extends MessageToMessageCodec<ByteBuf, RPCRequestMessage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, RPCRequestMessage msg, List<Object> out) {
        ByteBuf buffer = ctx.alloc().buffer();
        // 1. 消息魔数 4 字节
        buffer.writeInt(RPCMessage.MSG_MAGIC_CODE);
        // 2. 消息版本号 1 字节
        buffer.writeByte(1);
        // 3. 消息序列化方式 1 字节
        byte serialType = msg.getSerialType();
        buffer.writeByte(serialType);
        // 4. 消息序号 4 字节
        buffer.writeInt(msg.getSeqId());
        // 5. 消息类型 2 字节
        buffer.writeShort(msg.messageType());
        // 6. 消息长度 4 字节
        byte[] serialize = MessageSerialTypeEnum.getByOrdinal(msg.getSerialType()).serialize(msg);
        buffer.writeInt(serialize.length);
        // 7. 消息体 长度不固定
        buffer.writeBytes(serialize);
        out.add(buffer);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) {
        int magicCode = buffer.readInt();
        if (magicCode != RPCMessage.MSG_MAGIC_CODE) {
            return;
        }
        @SuppressWarnings("unused")
        byte version = buffer.readByte();
        byte serialType = buffer.readByte();
        @SuppressWarnings("unused")
        int seqId = buffer.readInt();
        short messageType = buffer.readShort();
        int messageLength = buffer.readInt();
        byte[] message = new byte[messageLength];
        buffer.readBytes(message);
        RPCMessage rpcMessage = MessageSerialTypeEnum.getByOrdinal(serialType).deserialize(message, messageType);
        out.add(rpcMessage);
    }
}
