package com.github.monkeywie.proxyee.server.auth;

import com.github.monkeywie.proxyee.server.auth.model.HttpToken;

/**
 * @Author LiWei
 * @Description
 * @Date 2021/1/15 14:12
 */
public interface HttpProxyAuthenticationProvider<R extends HttpToken> {
    String authType();

    String authRealm();

    R authenticate(String authorization);
}
