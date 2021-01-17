package com.github.monkeywie.proxyee.handler;

import com.github.monkeywie.proxyee.exception.HttpProxyExceptionHandle;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptPipeline;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.ReferenceCountUtil;

public class HttpProxyClientHandler extends ChannelInboundHandlerAdapter {

    private Channel clientChannel;

    public HttpProxyClientHandler(Channel clientChannel) {
        this.clientChannel = clientChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //客户端channel已关闭则不转发了
        if (!clientChannel.isOpen()) {
            ReferenceCountUtil.release(msg);
            return;
        }
        HttpProxyInterceptPipeline interceptPipeline = ((HttpProxyServerHandler) clientChannel.pipeline()
                .get("serverHandle")).getInterceptPipeline();
        if (msg instanceof HttpResponse) {
            interceptPipeline.afterResponse(clientChannel, ctx.channel(), (HttpResponse) msg);
        } else if (msg instanceof HttpContent) {
            interceptPipeline.afterResponse(clientChannel, ctx.channel(), (HttpContent) msg);
        } else {
            clientChannel.writeAndFlush(msg);
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().close();
        clientChannel.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().close();
        clientChannel.close();
        HttpProxyExceptionHandle exceptionHandle = ((HttpProxyServerHandler) clientChannel.pipeline()
                .get("serverHandle")).getExceptionHandle();
        exceptionHandle.afterCatch(clientChannel, ctx.channel(), cause);
    }
}
