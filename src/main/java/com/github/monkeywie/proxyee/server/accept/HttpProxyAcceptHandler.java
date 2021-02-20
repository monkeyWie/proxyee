package com.github.monkeywie.proxyee.server.accept;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;

/**
 * @Author LiWei
 * @Description
 * @Date 2021/2/20 10:22
 */
public interface HttpProxyAcceptHandler {
    /**
     * 当客户端有新的连接进来时会触发此方法
     *
     * @param request
     * @param clientChannel
     * @return 返回true表示放行，返回false则断开连接
     */
    boolean onAccept(HttpRequest request, Channel clientChannel);
}
