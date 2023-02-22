package com.github.cloudgyb.rpc.protocal;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * @author geng
 * @since 2023/02/22 11:25:45
 */
@ChannelHandler.Sharable
public class RPCProtocolCodec extends ChannelInboundHandlerAdapter {
    private final RPCMessageCodec rpcMessageCodec = new RPCMessageCodec();

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        ctx.pipeline()
                .addLast(new LengthFieldBasedFrameDecoder(2048,
                        12, 4,
                        0, 0))
                .addLast(rpcMessageCodec);
        ctx.pipeline().remove(this);
    }
}
