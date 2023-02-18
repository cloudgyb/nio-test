package com.github.cloudgyb.im.message;

/**
 * @author geng
 * @since 2023/02/17 21:06:01
 */

public class ChatResponseMessage extends Message {
    private String from;
    private String to;
    private boolean isSuccess;
    private String reason;

    public ChatResponseMessage(String from, String to, boolean isSuccess, String reason) {
        this.from = from;
        this.to = to;
        this.isSuccess = isSuccess;
        this.reason = reason;
    }

    @Override
    public int getMessageType() {
        return Message.CHAT_RESPONSE_MESSAGE;
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

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
