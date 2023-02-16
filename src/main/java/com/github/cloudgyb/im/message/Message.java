package com.github.cloudgyb.im.message;

import java.io.Serializable;

/**
 * @author geng
 * @since 2023/02/16 20:44:56
 */
public abstract class Message implements Serializable {
    public static int magicNum = 0xC0C0B0B0;
    public static int LOGIN_REQUEST_MESSAGE = 0;
    public static int LOGIN_RESPONSE_MESSAGE = 1;

    public static int CHAT_REQUEST_MESSAGE = 2;
    public static int CHAT_RESPONSE_MESSAGE = 3;

    public abstract int getMessageType();

    public static byte serial_jdk = 0;
    public static byte serial_json = 1;
    public static byte serial_protobuf = 2;

}
