package com.github.cloudgyb.im.message;

/**
 * @author geng
 * @since 2023/02/16 20:49:34
 */
public class LoginResponseMessage extends Message {
    private String username;
    private boolean isSuccess;
    private String reason;

    @Override
    public int getMessageType() {
        return Message.LOGIN_RESPONSE_MESSAGE;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    @Override
    public String toString() {
        return "LoginResponseMessage{" +
                "username='" + username + '\'' +
                ", isSuccess=" + isSuccess +
                ", reason='" + reason + '\'' +
                '}';
    }
}
