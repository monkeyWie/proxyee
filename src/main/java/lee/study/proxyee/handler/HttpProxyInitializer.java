package lee.study.proxyee.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.proxy.ProxyHandler;
import lee.study.proxyee.server.HttpProxyServer;
import lee.study.proxyee.util.ProtoUtil;
import lee.study.proxyee.util.ProtoUtil.RequestProto;

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
      ch.pipeline().addLast(HttpProxyServer.clientSslCtx.newHandler(ch.alloc(),requestProto.getHost(),requestProto.getPort()));
    }
    ch.pipeline().addLast("httpCodec", new HttpClientCodec());
    ch.pipeline().addLast(new HttpProxyClientHandle(clientChannel));
  }
}
