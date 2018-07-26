package com.github.monkeywie.proxyee.handler;

import com.github.monkeywie.proxyee.util.ProtoUtil.RequestProto;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.proxy.ProxyHandler;

/**
 * HTTP代理，转发解码后的HTTP报文
 */
public class HttpProxyInitializer extends ChannelInitializer {

  private Channel clientChannel;
  private RequestProto requestProto;
  private ProxyHandler proxyHandler;

  public HttpProxyInitializer(Channel clientChannel, RequestProto requestProto,
      ProxyHandler proxyHandler) {
    this.clientChannel = clientChannel;
    this.requestProto = requestProto;
    this.proxyHandler = proxyHandler;
  }

  @Override
  protected void initChannel(Channel ch) throws Exception {
    if (proxyHandler != null) {
      ch.pipeline().addLast(proxyHandler);
    }
    if (requestProto.getSsl()) {
      ch.pipeline().addLast(
          ((HttpProxyServerHandle) clientChannel.pipeline().get("serverHandle")).getServerConfig()
              .getClientSslCtx()
              .newHandler(ch.alloc(), requestProto.getHost(), requestProto.getPort()));
    }
    ch.pipeline().addLast("httpCodec", new HttpClientCodec());
    ch.pipeline().addLast("proxyClientHandle", new HttpProxyClientHandle(clientChannel));
  }
}
