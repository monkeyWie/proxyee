package lee.study;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.handler.proxy.Socks4ProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import io.netty.resolver.NoopAddressResolverGroup;
import io.netty.util.NetUtil;

import io.netty.util.concurrent.Future;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class TestProxyClient {

  public static void main(String[] args) throws InterruptedException {
    Bootstrap bootstrap = new Bootstrap();
    bootstrap.group(new NioEventLoopGroup()) // 注册线程池
        .channel(NioSocketChannel.class) // 使用NioSocketChannel来作为连接用的channel类
        .resolver(NoopAddressResolverGroup.INSTANCE)
        .handler(new ChannelInitializer() {

          @Override
          protected void initChannel(Channel ch) throws Exception {
//            ch.pipeline().addLast(new MyHttpProxyHandler("127.0.0.1",8888));
//            ch.pipeline().addLast(new HttpProxyHandler(new InetSocketAddress("127.0.0.1", 8888)));
            //ch.pipeline().addLast(new Socks5ProxyHandler(new InetSocketAddress("127.0.0.1", 1085)));
            ch.pipeline().addLast(new HttpClientCodec());
            ch.pipeline().addLast(new HttpObjectAggregator(81920000));
            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
              @Override
              public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                System.out.println(msg.toString());
              }
            });
          }
        });
    ChannelFuture cf = bootstrap.connect("www.baidu.com", 80).sync();
    HttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,
        "/");
    httpRequest.headers().add(HttpHeaderNames.HOST, "www.baidu.com");
    httpRequest.headers().add(HttpHeaderNames.USER_AGENT, "Proxyee Test");
    httpRequest.headers().add(HttpHeaderNames.CONTENT_LENGTH, 0);
    System.out.println(httpRequest.toString());
    cf.channel().writeAndFlush(httpRequest);
  }
}
