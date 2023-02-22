package com.github.cloudgyb.rpc.protocal;

import com.github.cloudgyb.rpc.message.RPCResponseMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.DefaultPromise;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author geng
 * @since 2023/02/22 14:43:59
 */
public class RPCResponseMessageHandler extends SimpleChannelInboundHandler<RPCResponseMessage> {
    private final Map<Integer, DefaultPromise<Object>> messagePromise = new ConcurrentHashMap<>();

    public void addMessagePromise(int messageSeqId, DefaultPromise<Object> promise) {
        messagePromise.put(messageSeqId, promise);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RPCResponseMessage msg) {
        int seqId = msg.getSeqId();
        boolean success = msg.isSuccess();
        DefaultPromise<Object> promise = messagePromise.get(seqId);
        if (!success) {
            Class<? extends Throwable> exception = msg.getException();
            System.out.println("抛出异常" + exception.getName());
            promise.setFailure(new Exception(exception.getName()));
        } else {
            promise.setSuccess(msg.getReturnValue());
        }
        // 执行结果已经放到 promise 中，将该消息 promise 移除
        messagePromise.remove(seqId);
        System.out.println(msg);
    }
}
