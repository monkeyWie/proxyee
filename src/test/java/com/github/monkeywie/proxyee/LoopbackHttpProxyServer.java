package com.github.monkeywie.proxyee;

import com.github.monkeywie.proxyee.server.HttpProxyServer;
import com.github.monkeywie.proxyee.server.HttpProxyServerConfig;
import io.netty.resolver.AddressResolver;
import io.netty.resolver.AddressResolverGroup;
import io.netty.resolver.DefaultNameResolver;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import io.netty.util.internal.SocketUtils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

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

    private static class LoopbackAddressResolverGroup extends AddressResolverGroup<InetSocketAddress> {
        public static final LoopbackAddressResolverGroup INSTANCE = new LoopbackAddressResolverGroup();

        private LoopbackAddressResolverGroup() {
        }

        @Override
        protected AddressResolver<InetSocketAddress> newResolver(EventExecutor executor) {
            return (new LoopbackNameResolver(executor)).asAddressResolver();
        }

        private static class LoopbackNameResolver extends DefaultNameResolver {

            public LoopbackNameResolver(EventExecutor executor) {
                super(executor);
            }

            @Override
            protected void doResolve(String inetHost, Promise<InetAddress> promise) {
                try {
                    promise.setSuccess(SocketUtils.addressByName("localhost"));
                } catch (UnknownHostException unknownHostException) {
                    promise.setFailure(unknownHostException);
                }
            }

            @Override
            protected void doResolveAll(String inetHost, Promise<List<InetAddress>> promise) {
                try {
                    promise.setSuccess(Arrays.asList(SocketUtils.allAddressesByName("localhost")));
                } catch (UnknownHostException unknownHostException) {
                    promise.setFailure(unknownHostException);
                }
            }
        }
    }


}
