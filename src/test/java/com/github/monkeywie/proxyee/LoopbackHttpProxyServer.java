package com.github.monkeywie.proxyee;

import com.github.monkeywie.proxyee.resolver.LoopbackAddressResolverGroup;
import com.github.monkeywie.proxyee.server.HttpProxyServer;
import com.github.monkeywie.proxyee.server.HttpProxyServerConfig;

public class LoopbackHttpProxyServer {

    public static void main(String[] args) {
        System.out.println("start loopback proxy server");
        HttpProxyServerConfig config = new HttpProxyServerConfig(LoopbackAddressResolverGroup.INSTANCE);
        config.setBossGroupThreads(1);
        config.setWorkerGroupThreads(1);
        config.setProxyGroupThreads(1);
        config.setHandleSsl(false);
        new HttpProxyServer()
                .serverConfig(config)
                .start(9999);
    }


}
