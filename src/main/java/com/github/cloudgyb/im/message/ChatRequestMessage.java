package com.github.cloudgyb.im.message;

/**
 * @author geng
 * @since 2023/02/17 21:06:01
 */

public class ChatRequestMessage extends Message {
    private String from;
    private String to;

    private String msg;

    public ChatRequestMessage(String from, String to, String msg) {
        this.from = from;
        this.to = to;
        this.msg = msg;
    }

    @Override
    public int getMessageType() {
        return Message.CHAT_REQUEST_MESSAGE;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "ChatRequestMessage{" +
                "from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", msg='" + msg + '\'' +
                '}';
    }
}
