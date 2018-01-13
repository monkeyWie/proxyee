package lee.study;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import java.security.KeyPair;
import java.security.Security;
import java.security.cert.X509Certificate;
import lee.study.proxyee.crt.CertPool;
import lee.study.proxyee.crt.CertUtil;
import lee.study.proxyee.exception.HttpProxyExceptionHandle;
import lee.study.proxyee.intercept.HttpProxyInterceptInitializer;
import lee.study.proxyee.server.HttpProxyServerConfig;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class TestServer {

  private static HttpProxyServerConfig serverConfig;

  static {
    try {
      //注册BouncyCastleProvider加密库
      Security.addProvider(new BouncyCastleProvider());
      if (serverConfig == null) {
        serverConfig = new HttpProxyServerConfig();
        serverConfig.setClientSslCtx(
            SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build());
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        X509Certificate certificate = CertUtil.loadCert(classLoader.getResourceAsStream("ca.crt"));
        //读取CA证书使用者信息
        serverConfig.setIssuer(CertUtil.getSubject(certificate));
        //读取CA证书有效时段(server证书有效期超出CA证书的，在手机上会提示证书不安全)
        serverConfig.setCaNotBefore(certificate.getNotBefore());
        serverConfig.setCaNotAfter(certificate.getNotAfter());
        //CA私钥用于给动态生成的网站SSL证书签证
        serverConfig
            .setCaPriKey(CertUtil.loadPriKey(classLoader.getResourceAsStream("ca_private.der")));
        //生产一对随机公私钥用于网站SSL证书动态创建
        KeyPair keyPair = CertUtil.genKeyPair();
        serverConfig.setServerPriKey(keyPair.getPrivate());
        serverConfig.setServerPubKey(keyPair.getPublic());
        serverConfig.setLoopGroup(new NioEventLoopGroup());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) throws Exception{
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    SslContext sslCtx = SslContextBuilder
        .forServer(serverConfig.getServerPriKey(), CertPool.getCert("www.test.com", serverConfig))
        .build();
    try {
      ServerBootstrap b = new ServerBootstrap();
      b.group(bossGroup, workerGroup)
          .channel(NioServerSocketChannel.class)
          .childHandler(new ChannelInitializer<Channel>() {

            @Override
            protected void initChannel(Channel ch) throws Exception {
              ch.pipeline().addFirst(sslCtx.newHandler(ch.alloc()));
              ch.pipeline().addLast(new HttpServerCodec());
              ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                  if(msg instanceof LastHttpContent){
                    String text = "test";
                    HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                        HttpResponseStatus.OK);
                    response.headers().set(HttpHeaderNames.CONTENT_LENGTH,text.getBytes().length);
                    response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html");
                    HttpContent content = new DefaultLastHttpContent();
                    content.content().writeBytes(text.getBytes());
                    ctx.channel().writeAndFlush(response);
                    ctx.channel().writeAndFlush(content);
                  }
                }

              });
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
                throws Exception {
              System.out.println("exceptionCaught:33333333");
              super.exceptionCaught(ctx, cause);
            }

            @Override
            public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
              System.out.println("channelUnregistered:33333333");
              super.channelUnregistered(ctx);
            }
          });
      ChannelFuture f = b
          .bind(8843)
          .sync();
      f.channel().closeFuture().sync();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      bossGroup.shutdownGracefully();
      workerGroup.shutdownGracefully();
    }
  }
}
