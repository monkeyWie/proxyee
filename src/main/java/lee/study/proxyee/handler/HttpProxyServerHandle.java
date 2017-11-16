package lee.study.proxyee.handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.resolver.NoopAddressResolverGroup;
import lee.study.proxyee.server.HttpProxyServer;
import lee.study.proxyee.crt.CertPool;
import lee.study.proxyee.intercept.HttpProxyIntercept;
import lee.study.proxyee.proxy.ProxyConfig;
import lee.study.proxyee.proxy.ProxyHandleFactory;
import lee.study.proxyee.util.ProtoUtil;

public class HttpProxyServerHandle extends ChannelInboundHandlerAdapter {

  private ChannelFuture cf;
  private String host;
  private int port;
  private boolean isSSL = false;
  private int status = 0;
  private HttpProxyIntercept httpProxyIntercept;
  private ProxyConfig proxyConfig;

  public HttpProxyServerHandle(HttpProxyIntercept httpProxyIntercept, ProxyConfig proxyConfig) {
    this.httpProxyIntercept = httpProxyIntercept;
    this.proxyConfig = proxyConfig;
  }

  public HttpProxyIntercept getHttpProxyIntercept() {
    return httpProxyIntercept;
  }

  @Override
  public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
    if (msg instanceof HttpRequest) {
      HttpRequest request = (HttpRequest) msg;
      //第一次建立连接取host和端口号和处理代理握手
      if (status == 0) {
        status = 1;
        ProtoUtil.RequestProto requestProto = ProtoUtil.getRequestProto(request);
        this.host = requestProto.getHost();
        this.port = requestProto.getPort();
        if ("CONNECT".equalsIgnoreCase(request.method().name())) {//建立代理握手
          status = 2;
          HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
              HttpProxyServer.SUCCESS);
          ctx.writeAndFlush(response);
          ctx.channel().pipeline().remove("httpCodec");
          return;
        }
      }
      if (!httpProxyIntercept.beforeRequest(ctx.channel(), request)) {
        return;
      }
      handleProxyData(ctx.channel(), msg, true);
    } else if (msg instanceof HttpContent) {
      if (status != 2) {
        if (!httpProxyIntercept.beforeRequest(ctx.channel(), (HttpContent) msg)) {
          return;
        }
        handleProxyData(ctx.channel(), msg, true);
      } else {
        status = 1;
      }
    } else { //ssl和websocket的握手处理
      ByteBuf byteBuf = (ByteBuf) msg;
      if (byteBuf.getByte(0) == 22) {//ssl握手
        isSSL = true;
        SslContext sslCtx = SslContextBuilder
            .forServer(HttpProxyServer.serverPriKey, CertPool.getCert(this.host)).build();
        ctx.pipeline().addFirst("httpCodec", new HttpServerCodec());
        ctx.pipeline().addFirst("sslHandle", sslCtx.newHandler(ctx.alloc()));
        //重新过一遍pipeline，拿到解密后的的http报文
        ctx.pipeline().fireChannelRead(msg);
        return;
      }
      handleProxyData(ctx.channel(), msg, false);
    }
  }

  @Override
  public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
    if (cf != null) {
      cf.channel().close();
    }
    ctx.channel().close();
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    if (cf != null) {
      cf.channel().close();
    }
    ctx.channel().close();
//        super.exceptionCaught(ctx, cause);
  }

  private void handleProxyData(final Channel channel, final Object msg, boolean isHttp)
      throws InterruptedException {
    if (cf == null) {
      ProxyHandler proxyHandler = ProxyHandleFactory.build(proxyConfig);
      ChannelInitializer channelInitializer =
          isHttp ? new HttpProxyInitializer(channel, isSSL, proxyHandler)
              : new TunnelProxyInitializer(channel, proxyHandler);
      Bootstrap bootstrap = new Bootstrap();
      bootstrap.group(HttpProxyServer.proxyGroup) // 注册线程池
          .channel(NioSocketChannel.class) // 使用NioSocketChannel来作为连接用的channel类
          .handler(channelInitializer);
      if (proxyConfig != null) {
        //代理服务器解析DNS和连接
        bootstrap.resolver(NoopAddressResolverGroup.INSTANCE);
      }
      cf = bootstrap.connect(host, port).sync();
            /*cf.addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture future) throws Exception {
                    System.out.println("11111"+msg);
                    if (future.isSuccess()) {
                        future.channel().writeAndFlush(msg);
                    } else {
                        ctx.channel().close();
                    }
                }
            });*/
    }
    cf.channel().writeAndFlush(msg);
  }

  public static void main(String[] args) {
    System.out.println((char) 22);
  }

}
