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
     * <div class="zh">客户端有新的连接建立时触发</div>
     * <div class="en">Triggered when the client has a new connection established</div>
     *
     * @param request
     * @param clientChannel
     * @return <div class="zh">返回true表示放行，返回false则断开连接</div>
     *         <div class="en">Return true to release, return false to disconnect</div>
     */
    boolean onAccept(HttpRequest request, Channel clientChannel);

    /**
     * <div class="zh">客户端连接关闭时触发</div>
     * <div class="en">Fired when the client connection is closed</div>
     *
     * @param clientChannel
     */
    void onClose(Channel clientChannel);
}
