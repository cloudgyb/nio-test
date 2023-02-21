package com.github.cloudgyb.rpc.message;

import java.io.Serializable;

/**
 * @author geng
 * @since 2023/02/21 21:00:05
 */
public abstract class RPCMessage implements Serializable {
    public static int MSG_MAGIC_CODE = 0xCCBAEA02;
    public static short RPC_REQUEST_MSG = 0;
    public static short RPC_RESPONSE_MSG = 1;
    protected int seqId;
    protected byte serialType;

    public int getSeqId() {
        return seqId;
    }

    public void setSeqId(int seqId) {
        this.seqId = seqId;
    }

    public byte getSerialType() {
        return serialType;
    }

    public void setSerialType(byte serialType) {
        this.serialType = serialType;
    }

    public abstract short messageType();
}
