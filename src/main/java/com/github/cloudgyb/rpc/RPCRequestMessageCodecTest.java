package com.github.cloudgyb.rpc;

import com.github.cloudgyb.rpc.message.MessageSerialTypeEnum;
import com.github.cloudgyb.rpc.message.RPCMessage;
import com.github.cloudgyb.rpc.message.RPCRequestMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LoggingHandler;

/**
 * @author geng
 * @since 2023/02/21 22:03:59
 */
public class RPCRequestMessageCodecTest {
    public static void main(String[] args) {
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(
                new LoggingHandler(),
                new LengthFieldBasedFrameDecoder(1024, 12, 4,
                        0, 0),
                new RPCRequestMessageCodec(),
                new LoggingHandler()
        );
        RPCRequestMessage msg = new RPCRequestMessage("com.github.cloudgyb.rpc.service.HelloService",
                "sayHello", new Class[]{String.class},
                String.class, new Object[]{"geng"});
        msg.setSeqId(1);
        msg.setSerialType((byte) MessageSerialTypeEnum.GSON.ordinal());
        embeddedChannel.writeOutbound(msg);

        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
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
        embeddedChannel.writeInbound(buffer);
        embeddedChannel.close();
    }
}
