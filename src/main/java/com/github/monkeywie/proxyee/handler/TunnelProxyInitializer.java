package com.github.monkeywie.proxyee.handler;

import com.github.monkeywie.proxyee.exception.HttpProxyExceptionHandle;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.proxy.ProxyHandler;

/**
 * <div class="zh">http代理隧道，转发原始报文</div>
 * <div class="en">http proxy tunnel, forwarding original packets</div>
 */
public class TunnelProxyInitializer extends ChannelInitializer {

    private Channel clientChannel;
    private ProxyHandler proxyHandler;

    public TunnelProxyInitializer(Channel clientChannel,
                                  ProxyHandler proxyHandler) {
        this.clientChannel = clientChannel;
        this.proxyHandler = proxyHandler;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        if (proxyHandler != null) {
            ch.pipeline().addLast(proxyHandler);
        }
        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx0, Object msg0) throws Exception {
                clientChannel.writeAndFlush(msg0);
            }

            @Override
            public void channelUnregistered(ChannelHandlerContext ctx0) throws Exception {
                ctx0.channel().close();
                clientChannel.close();
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx0, Throwable cause) throws Exception {
                ctx0.channel().close();
                clientChannel.close();
                HttpProxyExceptionHandle exceptionHandle = ((HttpProxyServerHandler) clientChannel.pipeline()
                        .get("serverHandle")).getExceptionHandle();
                exceptionHandle.afterCatch(clientChannel, ctx0.channel(), cause);
            }
        });
    }
}
