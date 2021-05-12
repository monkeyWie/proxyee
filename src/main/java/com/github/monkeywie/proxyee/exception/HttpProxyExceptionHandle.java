package com.github.monkeywie.proxyee.exception;

import io.netty.channel.Channel;

public class HttpProxyExceptionHandle {

    public void startCatch(Throwable e) {
        e.printStackTrace();
    }

    public void beforeCatch(Channel clientChannel, Throwable cause) throws Exception {
    }

    public void afterCatch(Channel clientChannel, Channel proxyChannel, Throwable cause)
            throws Exception {
    }
}
