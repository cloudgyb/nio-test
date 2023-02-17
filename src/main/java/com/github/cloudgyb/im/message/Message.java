package com.github.cloudgyb.im.message;

import java.io.Serializable;

/**
 * @author geng
 * @since 2023/02/16 20:44:56
 */
public abstract class Message implements Serializable {
    public static short magicNum = (short) 0xC0B0;
    public static int LOGIN_REQUEST_MESSAGE = 0;
    public static int LOGIN_RESPONSE_MESSAGE = 1;

    public static int CHAT_REQUEST_MESSAGE = 2;
    public static int CHAT_RESPONSE_MESSAGE = 3;

    protected Message(long seqId) {
        this.seqId = seqId;
    }

    public abstract int getMessageType();

    private final long seqId;

    public long getSeqId() {
        return seqId;
    }

    public static byte serial_jdk = 0;
    public static byte serial_json = 1;
    public static byte serial_protobuf = 2;

}
