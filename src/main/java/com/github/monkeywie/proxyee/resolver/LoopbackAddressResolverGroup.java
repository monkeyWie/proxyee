package com.github.monkeywie.proxyee.resolver;

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

public final class LoopbackAddressResolverGroup extends AddressResolverGroup<InetSocketAddress> {
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