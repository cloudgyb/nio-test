package com.github.cloudgyb.rpc.message;

import java.util.Arrays;

/**
 * rpc 请求消息
 *
 * @author geng
 * @since 2023/02/21 20:43:30
 */
public class RPCRequestMessage extends RPCMessage {
    private String service;
    private String method;
    private Class<?>[] paramsTypes;
    private Class<?> returnType;
    private Object[] paramsValues;

    public RPCRequestMessage() {
    }

    public RPCRequestMessage(String service, String method, Class<?>[] paramsTypes, Class<?> returnType, Object[] paramsValues) {
        this.service = service;
        this.method = method;
        this.paramsTypes = paramsTypes;
        this.returnType = returnType;
        this.paramsValues = paramsValues;
    }

    @Override
    public short messageType() {
        return RPC_REQUEST_MSG;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Class<?>[] getParamsTypes() {
        return paramsTypes;
    }

    public void setParamsTypes(Class<?>[] paramsTypes) {
        this.paramsTypes = paramsTypes;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public void setReturnType(Class<?> returnType) {
        this.returnType = returnType;
    }

    public Object[] getParamsValues() {
        return paramsValues;
    }

    public void setParamsValues(Object[] paramsValues) {
        this.paramsValues = paramsValues;
    }

    @Override
    public String toString() {
        return "RPCRequestMessage{" +
                "service='" + service + '\'' +
                ", method='" + method + '\'' +
                ", paramsTypes=" + Arrays.toString(paramsTypes) +
                ", returnType=" + returnType +
                ", paramsValues=" + Arrays.toString(paramsValues) +
                ", seqId=" + seqId +
                ", serialType=" + serialType +
                '}';
    }
}
