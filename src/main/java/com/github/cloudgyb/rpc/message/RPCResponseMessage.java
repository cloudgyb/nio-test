package com.github.cloudgyb.rpc.message;

/**
 * RPC 响应消息
 *
 * @author geng
 * @since 2023/02/22 11:35:20
 */
public class RPCResponseMessage extends RPCMessage {
    private String service;
    private Class<?> returnType;
    private Object returnValue;

    private boolean isSuccess;
    private Class<? extends Throwable> exception;

    public RPCResponseMessage() {
    }

    public RPCResponseMessage(String service, Class<?> returnType, Object returnValue) {
        this.service = service;
        this.returnType = returnType;
        this.returnValue = returnValue;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public void setReturnType(Class<?> returnType) {
        this.returnType = returnType;
    }

    public Object getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(Object returnValue) {
        this.returnValue = returnValue;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public Class<? extends Throwable> getException() {
        return exception;
    }

    public void setException(Class<? extends Throwable> exception) {
        this.exception = exception;
    }

    @Override
    public short messageType() {
        return RPCMessage.RPC_RESPONSE_MSG;
    }

    @Override
    public String toString() {
        return "RPCResponseMessage{" +
                "service='" + service + '\'' +
                ", returnType=" + returnType +
                ", returnValue=" + returnValue +
                ", isSuccess=" + isSuccess +
                ", exception=" + exception +
                ", seqId=" + seqId +
                ", serialType=" + serialType +
                '}';
    }
}
