package com.github.monkeywie.proxyee;

import com.github.monkeywie.proxyee.server.HttpProxyServer;
import com.github.monkeywie.proxyee.server.HttpProxyServerConfig;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class AcceptHttpProxyServer {

    private static final Map<String, Integer> CLIENT_LIMIT_MAP = new HashMap<>();

    public static void main(String[] args) throws Exception {
        HttpProxyServerConfig config = new HttpProxyServerConfig();
        config.setHttpProxyAcceptHandler((request, clientChannel) -> {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) clientChannel.localAddress();
            String ip = inetSocketAddress.getHostString();
            Integer count = CLIENT_LIMIT_MAP.getOrDefault(ip, 1);
            if (count > 5) {
                FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN);
                fullHttpResponse.content().writeBytes("<html><div>访问过于频繁！</div></html>".getBytes());
                clientChannel.writeAndFlush(fullHttpResponse);
                return false;
            }
            CLIENT_LIMIT_MAP.put(ip, count + 1);
            return true;
        });
        new HttpProxyServer()
                .serverConfig(config)
                .start(9999);
    }
}
