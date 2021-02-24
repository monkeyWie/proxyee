package com.github.monkeywie.proxyee;

import com.github.monkeywie.proxyee.server.HttpProxyServer;
import com.github.monkeywie.proxyee.server.HttpProxyServerConfig;
import com.github.monkeywie.proxyee.server.accept.HttpProxyAcceptHandler;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.*;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AcceptHttpProxyServer {

    private static final Map<String, Integer> CLIENT_LIMIT_MAP = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        HttpProxyServerConfig config = new HttpProxyServerConfig();
        config.setHttpProxyAcceptHandler(new HttpProxyAcceptHandler() {
            @Override
            public boolean onAccept(final HttpRequest request, final Channel clientChannel) {
                String ip = getClientIp(clientChannel);
                Integer count = CLIENT_LIMIT_MAP.getOrDefault(ip, 1);
                if (count > 5) {
                    FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN);
                    fullHttpResponse.content().writeBytes("<html><div>访问过于频繁！</div></html>".getBytes());
                    clientChannel.writeAndFlush(fullHttpResponse);
                    return false;
                }
                CLIENT_LIMIT_MAP.put(ip, count + 1);
                return true;
            }

            @Override
            public void onClose(final Channel clientChannel) {
                CLIENT_LIMIT_MAP.computeIfPresent(getClientIp(clientChannel), (s, count) -> {
                    if (count > 0) {
                        return count - 1;
                    }
                    return count;
                });
            }

            private String getClientIp(Channel clientChannel) {
                InetSocketAddress inetSocketAddress = (InetSocketAddress) clientChannel.localAddress();
                return inetSocketAddress.getHostString();
            }
        });
        new HttpProxyServer()
                .serverConfig(config)
                .start(9999);
    }
}
