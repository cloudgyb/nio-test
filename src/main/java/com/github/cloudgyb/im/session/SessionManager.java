package com.github.cloudgyb.im.session;


import io.netty.channel.Channel;

/**
 * @author geng
 * @since 2023/02/17 22:36:32
 */
public interface SessionManager {
    SessionManager DEFAULT = new DefaultSessionManager();

    Channel getSession(String username);

    void saveSession(String username, Channel channel);

    void removeSession(String username);
}
