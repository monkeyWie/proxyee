package com.github.monkeywie.proxyee;

import com.github.monkeywie.proxyee.resolver.LoopbackAddressResolverGroup;
import com.github.monkeywie.proxyee.server.HttpProxyServer;
import com.github.monkeywie.proxyee.server.HttpProxyServerConfig;

public class LoopbackProxyServerApp {
    public static void main(String[] args) {
        System.out.println("start loopback proxy server");
        int port = 9999;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        new HttpProxyServer()
                .serverConfig(new HttpProxyServerConfig(LoopbackAddressResolverGroup.INSTANCE))
                .start(port);
    }
}
