package lee.study.proxyee.handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.resolver.NoopAddressResolverGroup;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import lee.study.proxyee.crt.CertPool;
import lee.study.proxyee.exception.HttpProxyExceptionHandle;
import lee.study.proxyee.intercept.HttpProxyIntercept;
import lee.study.proxyee.intercept.HttpProxyInterceptInitializer;
import lee.study.proxyee.intercept.HttpProxyInterceptPipeline;
import lee.study.proxyee.proxy.ProxyConfig;
import lee.study.proxyee.proxy.ProxyHandleFactory;
import lee.study.proxyee.server.HttpProxyServer;
import lee.study.proxyee.server.HttpProxyServerConfig;
import lee.study.proxyee.util.ProtoUtil;
import lee.study.proxyee.util.ProtoUtil.RequestProto;

public class HttpProxyServerHandle extends ChannelInboundHandlerAdapter {

  private ChannelFuture cf;
  private String host;
  private int port;
  private boolean isSsl = false;
  private int status = 0;
  private HttpProxyServerConfig serverConfig;
  private ProxyConfig proxyConfig;
  private HttpProxyInterceptPipeline interceptPipeline;
  private HttpProxyExceptionHandle exceptionHandle;
  private List requestList;
  private boolean isConnect;

  public HttpProxyServerConfig getServerConfig() {
    return serverConfig;
  }

  public HttpProxyInterceptPipeline getInterceptPipeline() {
    return interceptPipeline;
  }

  public HttpProxyExceptionHandle getExceptionHandle() {
    return exceptionHandle;
  }

  public HttpProxyServerHandle(HttpProxyServerConfig serverConfig,
      HttpProxyInterceptInitializer interceptInitializer,
      ProxyConfig proxyConfig, HttpProxyExceptionHandle exceptionHandle) {
    this.serverConfig = serverConfig;
    this.proxyConfig = proxyConfig;

    //默认拦截器
    this.interceptPipeline = new HttpProxyInterceptPipeline(new HttpProxyIntercept() {
      @Override
      public void beforeRequest(Channel clientChannel, HttpRequest httpRequest,
          HttpProxyInterceptPipeline pipeline) throws Exception {
        handleProxyData(clientChannel, httpRequest, true);
      }

      @Override
      public void beforeRequest(Channel clientChannel, HttpContent httpContent,
          HttpProxyInterceptPipeline pipeline) throws Exception {
        handleProxyData(clientChannel, httpContent, true);
      }

      @Override
      public void afterResponse(Channel clientChannel, Channel proxyChannel,
          HttpResponse httpResponse, HttpProxyInterceptPipeline pipeline) throws Exception {
        clientChannel.writeAndFlush(httpResponse);
        if (HttpHeaderValues.WEBSOCKET.toString()
            .equals(httpResponse.headers().get(HttpHeaderNames.UPGRADE))) {
          //websocket转发原始报文
          proxyChannel.pipeline().remove("httpCodec");
          clientChannel.pipeline().remove("httpCodec");
        }
      }

      @Override
      public void afterResponse(Channel clientChannel, Channel proxyChannel,
          HttpContent httpContent, HttpProxyInterceptPipeline pipeline) throws Exception {
        clientChannel.writeAndFlush(httpContent);
      }
    });
    interceptInitializer.init(this.interceptPipeline);

    this.exceptionHandle = exceptionHandle;
  }

  @Override
  public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
    if (msg instanceof HttpRequest) {
      HttpRequest request = (HttpRequest) msg;
      //第一次建立连接取host和端口号和处理代理握手
      if (status == 0) {
        RequestProto requestProto = ProtoUtil.getRequestProto(request);
        if (requestProto == null) { //bad request
          ctx.channel().close();
          return;
        }
        status = 1;
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
      interceptPipeline.setRequestProto(new RequestProto(host, port, isSsl));
      interceptPipeline.beforeRequest(ctx.channel(), request);
    } else if (msg instanceof HttpContent) {
      if (status != 2) {
        interceptPipeline.beforeRequest(ctx.channel(), (HttpContent) msg);
      } else {
        status = 1;
      }
    } else { //ssl和websocket的握手处理
      ByteBuf byteBuf = (ByteBuf) msg;
      if (byteBuf.getByte(0) == 22) {//ssl握手
        isSsl = true;
        SslContext sslCtx = SslContextBuilder
            .forServer(serverConfig.getServerPriKey(), CertPool.getCert(this.host, serverConfig))
            .build();
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
    exceptionHandle.beforeCatch(ctx.channel(), cause);
  }

  private void handleProxyData(Channel channel, Object msg, boolean isHttp)
      throws Exception {
    if (cf == null) {
      if (isHttp && !(msg instanceof HttpRequest)) {  //connection异常 还有HttpContent进来，不转发
        return;
      }
      ProxyHandler proxyHandler = ProxyHandleFactory.build(proxyConfig);
      /*
        添加SSL client hello的Server Name Indication extension(SNI扩展)
        有些服务器对于client hello不带SNI扩展时会直接返回Received fatal alert: handshake_failure(握手错误)
        例如：https://cdn.mdn.mozilla.net/static/img/favicon32.7f3da72dcea1.png
       */
      RequestProto requestProto = new RequestProto(host, port, isSsl);
      ChannelInitializer channelInitializer =
          isHttp ? new HttpProxyInitializer(channel, requestProto, proxyHandler)
              : new TunnelProxyInitializer(channel, proxyHandler);
      Bootstrap bootstrap = new Bootstrap();
      bootstrap.group(serverConfig.getLoopGroup()) // 注册线程池
          .channel(NioSocketChannel.class) // 使用NioSocketChannel来作为连接用的channel类
          .handler(channelInitializer);
      if (proxyConfig != null) {
        //代理服务器解析DNS和连接
        bootstrap.resolver(NoopAddressResolverGroup.INSTANCE);
      }
      requestList = new LinkedList();
      cf = bootstrap.connect(host, port);
      cf.addListener((ChannelFutureListener) future -> {
        if (future.isSuccess()) {
          future.channel().writeAndFlush(msg);
          synchronized (requestList){
            requestList.forEach((obj)-> future.channel().write(obj));
            isConnect = true;
          }
        } else {
          future.channel().close();
          channel.close();
        }
      });
    }else {
      synchronized (requestList){
        if(isConnect){
          cf.channel().writeAndFlush(msg);
        }else {
          requestList.add(msg);
        }
      }
    }
  }

}
