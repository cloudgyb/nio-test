package com.github.cloudgyb.im.session;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author geng
 * @since 2023/02/17 22:38:31
 */
public class DefaultSessionManager implements SessionManager {
    private final Map<String, Channel> sessions = new ConcurrentHashMap<>();

    @Override
    public Channel getSession(String username) {
        return sessions.get(username);
    }

    @Override
    public void saveSession(String username, Channel channel) {
        sessions.put(username, channel);
    }

    @Override
    public void removeSession(String username) {

    }
}
