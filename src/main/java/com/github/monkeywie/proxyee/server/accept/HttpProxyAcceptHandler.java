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
     * 客户端有新的连接建立时触发
     *
     * @param request
     * @param clientChannel
     * @return 返回true表示放行，返回false则断开连接
     */
    boolean onAccept(HttpRequest request, Channel clientChannel);

    /**
     * 客户端连接关闭时触发
     *
     * @param clientChannel
     */
    void onClose(Channel clientChannel);
}
