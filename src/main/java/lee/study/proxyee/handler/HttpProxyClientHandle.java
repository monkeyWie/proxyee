package lee.study.proxyee.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import lee.study.proxyee.intercept.HttpProxyIntercept;

public class HttpProxyClientHandle extends ChannelInboundHandlerAdapter {

  private Channel clientChannel;
  private HttpProxyIntercept httpProxyIntercept;

  public HttpProxyClientHandle(Channel clientChannel) {
    this.clientChannel = clientChannel;
    this.httpProxyIntercept = ((HttpProxyServerHandle) clientChannel.pipeline().get("serverHandle"))
        .getHttpProxyIntercept();
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    HttpResponse response = null;
    if (msg instanceof HttpResponse) {
      response = (HttpResponse) msg;
      if (!httpProxyIntercept.afterResponse(clientChannel, ctx.channel(), response)) {
        return;
      }
    } else if (msg instanceof HttpContent) {
      if (!httpProxyIntercept.afterResponse(clientChannel, ctx.channel(), (HttpContent) msg)) {
        return;
      }
    }
    clientChannel.writeAndFlush(msg);
    if (response != null) {
      if (HttpHeaderValues.WEBSOCKET.toString()
          .equals(response.headers().get(HttpHeaderNames.UPGRADE))) {
        //websocket转发原始报文
        ctx.pipeline().remove("httpCodec");
        clientChannel.pipeline().remove("httpCodec");
      }

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
    super.exceptionCaught(ctx, cause);
  }
}
