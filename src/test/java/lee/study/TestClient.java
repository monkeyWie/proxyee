package lee.study;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.ssl.OpenSslEngine;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import java.util.Arrays;
import javax.net.ssl.SSLParameters;

public class TestClient {

  public static void main(String[] args) throws InterruptedException {
    Bootstrap bootstrap = new Bootstrap();
    bootstrap.group(new NioEventLoopGroup()) // 注册线程池
        .channel(NioSocketChannel.class) // 使用NioSocketChannel来作为连接用的channel类
        .handler(new ChannelInitializer() {

          @Override
          protected void initChannel(Channel ch) throws Exception {
            ch.pipeline().addLast(
                SslContextBuilder
                    .forClient()
//                    .protocols("TLSv1")
                    .trustManager(
                InsecureTrustManagerFactory.INSTANCE)
                .build()
                    .newHandler(
                        ch.alloc(),"cdn.mdn.mozilla.net",443
                    )
            );
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
    //https://cdn.mdn.mozilla.net/static/img/favicon32.7f3da72dcea1.png
    ChannelFuture cf = bootstrap.connect("cdn.mdn.mozilla.net", 443).sync();
    HttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,
        "/static/img/favicon32.7f3da72dcea1.png");
    httpRequest.headers().add(HttpHeaderNames.HOST, "cdn.mdn.mozilla.net");
    httpRequest.headers().add(HttpHeaderNames.USER_AGENT, "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.75 Safari/537.36");
    httpRequest.headers().add(HttpHeaderNames.CONTENT_LENGTH, 0);
    System.out.println(httpRequest.toString());
    cf.channel().writeAndFlush(httpRequest);
  }
}
