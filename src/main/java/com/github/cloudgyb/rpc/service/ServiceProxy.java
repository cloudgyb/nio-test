package com.github.cloudgyb.rpc.service;

import com.github.cloudgyb.rpc.message.MessageSerialTypeEnum;
import com.github.cloudgyb.rpc.message.RPCRequestMessage;
import com.github.cloudgyb.rpc.protocal.RPCResponseMessageHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author geng
 * @since 2023/02/22 15:09:03
 */
public class ServiceProxy implements InvocationHandler {
    private final static AtomicInteger invokeCount = new AtomicInteger(0);
    private final Class<?> proxyInterface;
    private final Channel channel;

    public ServiceProxy(Class<?> proxyInterface, Channel channel) {
        this.proxyInterface = proxyInterface;
        this.channel = channel;
    }

    @SuppressWarnings("unchecked")
    public static <T> T newProxy(Class<T> proxyInterface, Channel channel) {
        return (T) Proxy.newProxyInstance(
                ServiceProxy.class.getClassLoader(),
                new Class[]{proxyInterface},
                new ServiceProxy(proxyInterface, channel)
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RPCRequestMessage rpcRequestMessage = new RPCRequestMessage(proxyInterface.getName(), method.getName(),
                method.getParameterTypes(), method.getReturnType(), args);
        rpcRequestMessage.setSeqId(invokeCount.getAndIncrement());
        rpcRequestMessage.setSerialType((byte) MessageSerialTypeEnum.GSON.ordinal());
        // 写入消息
        channel.writeAndFlush(rpcRequestMessage);
        // 获取响应消息 handler，用于建立消息与promise 的对应关系
        ChannelHandlerContext context = channel.pipeline().context(RPCResponseMessageHandler.class);
        RPCResponseMessageHandler handler = (RPCResponseMessageHandler) context.handler();
        DefaultPromise<Object> promise = new DefaultPromise<>(GlobalEventExecutor.INSTANCE);
        handler.addMessagePromise(rpcRequestMessage.getSeqId(), promise);
        return promise.get();
    }
}
