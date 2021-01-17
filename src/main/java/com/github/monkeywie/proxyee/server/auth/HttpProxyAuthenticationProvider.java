package com.github.monkeywie.proxyee.server.auth;

/**
 * @Author LiWei
 * @Description
 * @Date 2021/1/15 14:12
 */
public interface HttpProxyAuthenticationProvider {
    String authType();

    String authRealm();

    boolean authenticate(String authorization);
}
