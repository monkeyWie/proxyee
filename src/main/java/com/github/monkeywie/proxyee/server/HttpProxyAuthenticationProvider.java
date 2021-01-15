package com.github.monkeywie.proxyee.server;

/**
 * @Author LiWei
 * @Description
 * @Date 2021/1/15 14:12
 */
public interface HttpProxyAuthenticationProvider {
    default boolean authenticate(String usr, String pwd) {
        return true;
    }
}
