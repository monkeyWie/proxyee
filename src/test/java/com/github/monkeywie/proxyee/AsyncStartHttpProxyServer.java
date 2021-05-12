package com.github.monkeywie.proxyee;

import com.github.monkeywie.proxyee.server.HttpProxyServer;
import com.github.monkeywie.proxyee.server.HttpProxyServerConfig;

/**
 * @author aomsweet
 */
public class AsyncStartHttpProxyServer {

    public static void main(String[] args) {
        HttpProxyServerConfig config = new HttpProxyServerConfig();
        config.setBossGroupThreads(1);
        new HttpProxyServer()
                .serverConfig(config)
                .startAsync(9999).whenComplete((result, cause) -> {
            if (cause != null) {
                cause.printStackTrace();
            }
        });
    }
}
