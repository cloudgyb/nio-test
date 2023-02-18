package com.github.cloudgyb.im.serivce;

import java.util.HashMap;
import java.util.Map;

/**
 * @author geng
 * @since 2023/02/17 21:35:11
 */
public class UserLoginService {
    private static final Map<String, String> userDB = new HashMap<>();

    static {
        userDB.put("geng", "123456");
        userDB.put("gyb", "123456");
        userDB.put("二哈", "123456");
    }

    public boolean login(String username, String password) {
        String password0 = userDB.get(username);
        return password.equals(password0);
    }

    public boolean userExist(String username) {
        return userDB.get(username) != null;
    }
}
