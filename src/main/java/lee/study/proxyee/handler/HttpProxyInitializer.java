package lee.study.proxyee.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.proxy.ProxyHandler;
import lee.study.proxyee.server.HttpProxyServer;

/**
 * HTTP代理，转发解码后的HTTP报文
 */
public class HttpProxyInitializer extends ChannelInitializer {

  private Channel clientChannel;
  private boolean isSSL;
  private ProxyHandler proxyHandler;

  public HttpProxyInitializer(Channel clientChannel, boolean isSSL,
      ProxyHandler proxyHandler) {
    this.clientChannel = clientChannel;
    this.isSSL = isSSL;
    this.proxyHandler = proxyHandler;
  }

  @Override
  protected void initChannel(Channel ch) throws Exception {
    if (proxyHandler != null) {
      ch.pipeline().addLast(proxyHandler);
    }
    if (isSSL) {
      ch.pipeline().addLast(HttpProxyServer.clientSslCtx.newHandler(ch.alloc()));
    }
    ch.pipeline().addLast("httpCodec", new HttpClientCodec());
    ch.pipeline().addLast(new HttpProxyClientHandle(clientChannel));
  }
}
