package com.github.monkeywie.proxyee.server.auth;

import com.github.monkeywie.proxyee.server.auth.model.HttpToken;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;

/**
 * @Author LiWei
 * @Description
 * @Date 2021/1/15 14:12
 */
public interface HttpProxyAuthenticationProvider<R extends HttpToken> {
    String authType();

    String authRealm();

    R authenticate(String authorization);

    default R authenticate(HttpRequest request) {
      return authenticate(request.headers().get(HttpHeaderNames.PROXY_AUTHORIZATION));
    }

    default boolean matches(HttpRequest request) {
        return true;
    }
}
