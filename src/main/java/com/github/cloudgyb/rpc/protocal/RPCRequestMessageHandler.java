package com.github.cloudgyb.rpc.protocal;

import com.github.cloudgyb.rpc.message.MessageSerialTypeEnum;
import com.github.cloudgyb.rpc.message.RPCRequestMessage;
import com.github.cloudgyb.rpc.message.RPCResponseMessage;
import com.github.cloudgyb.rpc.service.ServiceFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.Method;

/**
 * @author geng
 * @since 2023/02/22 11:39:21
 */
@ChannelHandler.Sharable
public class RPCRequestMessageHandler extends SimpleChannelInboundHandler<RPCRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RPCRequestMessage msg) {
        String service = msg.getService();
        String methodName = msg.getMethod();
        Class<?>[] paramsTypes = msg.getParamsTypes();
        Object[] paramsValues = msg.getParamsValues();
        Class<?> returnType = msg.getReturnType();
        Object invokeRes = null;
        RPCResponseMessage rpcResponseMessage = new RPCResponseMessage(service, returnType, invokeRes);
        rpcResponseMessage.setSeqId(msg.getSeqId());
        rpcResponseMessage.setSerialType((byte) MessageSerialTypeEnum.GSON.ordinal());
        try {
            Class<?> serviceClass = Class.forName(service);
            Object serviceInstance = ServiceFactory.getServiceInstance(serviceClass);
            Method method = serviceClass.getMethod(methodName, paramsTypes);
            invokeRes = method.invoke(serviceInstance, paramsValues);
            rpcResponseMessage.setSuccess(true);
            rpcResponseMessage.setReturnValue(invokeRes);
        } catch (Exception e) {
            rpcResponseMessage.setSuccess(false);
            rpcResponseMessage.setException(e.getClass());
        }
        ctx.writeAndFlush(rpcResponseMessage);
    }
}
