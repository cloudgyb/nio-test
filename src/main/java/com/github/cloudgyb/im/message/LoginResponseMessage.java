package com.github.cloudgyb.im.message;

/**
 * @author geng
 * @since 2023/02/16 20:49:34
 */
public class LoginResponseMessage extends Message {
    private String username;
    private int code;
    private String msg;

    public LoginResponseMessage(long seqId) {
        super(seqId);
    }

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

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
