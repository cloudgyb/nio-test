package com.github.cloudgyb.im.message;

/**
 * @author geng
 * @since 2023/02/16 20:49:34
 */
public class LoginRequestMessage extends Message {
    private String username;
    private String password;

    public LoginRequestMessage(long seqId) {
        super(seqId);
    }

    @Override
    public int getMessageType() {
        return Message.LOGIN_REQUEST_MESSAGE;
    }

    @Override
    public long getSeqId() {
        return 0;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "LoginRequestMessage{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
