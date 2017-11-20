package lee.study.proxyee.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.AttributeKey;
import lee.study.proxyee.crt.CertUtil;
import lee.study.proxyee.handler.HttpProxyServerHandle;
import lee.study.proxyee.intercept.DefaultInterceptFactory;
import lee.study.proxyee.intercept.HttpProxyIntercept;
import lee.study.proxyee.intercept.ProxyInterceptFactory;
import lee.study.proxyee.proxy.ProxyConfig;
import lee.study.proxyee.proxy.ProxyType;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;

public class HttpProxyServer {

  //http代理隧道握手成功
  public final static HttpResponseStatus SUCCESS = new HttpResponseStatus(200,
      "Connection established");

  public static SslContext clientSslCtx;
  public static String issuer;
  public static PrivateKey caPriKey;
  public static PrivateKey serverPriKey;
  public static PublicKey serverPubKey;
  public static EventLoopGroup proxyGroup;

  private ProxyInterceptFactory proxyInterceptFactory;
  private ProxyConfig proxyConfig;

  private void init() throws Exception {
    //注册BouncyCastleProvider加密库
    Security.addProvider(new BouncyCastleProvider());
    clientSslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE)
        .build();
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    //读取CA证书使用者信息
    issuer = CertUtil.getSubject(classLoader.getResourceAsStream("ca.crt"));
    //CA私钥用于给动态生成的网站SSL证书签证
    caPriKey = CertUtil.loadPriKey(classLoader.getResourceAsStream("ca_private.pem"));
    //生产一对随机公私钥用于网站SSL证书动态创建
    KeyPair keyPair = CertUtil.genKeyPair();
    serverPriKey = keyPair.getPrivate();
    serverPubKey = keyPair.getPublic();
    proxyGroup = new NioEventLoopGroup();
    if (proxyInterceptFactory == null) {
      proxyInterceptFactory = new DefaultInterceptFactory();
    }
  }

  public HttpProxyServer proxyInterceptFactory(
      ProxyInterceptFactory proxyInterceptFactory) {
    this.proxyInterceptFactory = proxyInterceptFactory;
    return this;
  }

  public HttpProxyServer proxyConfig(ProxyConfig proxyConfig) {
    this.proxyConfig = proxyConfig;
    return this;
  }

  public void start(int port) {

    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    try {
      init();
      ServerBootstrap b = new ServerBootstrap();
      b.group(bossGroup, workerGroup)
          .channel(NioServerSocketChannel.class)
//        .option(ChannelOption.SO_BACKLOG, 100)
//                    .handler(new LoggingHandler(LogLevel.ERROR))
          .childHandler(new ChannelInitializer<Channel>() {

            @Override
            protected void initChannel(Channel ch) throws Exception {
              ch.pipeline().addLast("httpCodec", new HttpServerCodec());
              ch.pipeline().addLast("serverHandle",
                  new HttpProxyServerHandle(proxyInterceptFactory.build(), proxyConfig));
            }
          });
      ChannelFuture f = b
          .bind(port)
          .sync();
      f.channel().closeFuture().sync();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      bossGroup.shutdownGracefully();
      workerGroup.shutdownGracefully();
    }
  }

  public static void main(String[] args) throws Exception {
  new HttpProxyServer().start(9999);

    /*new HttpProxyServer()
//        .proxyConfig(new ProxyConfig(ProxyType.SOCKS5, "127.0.0.1", 1085))  //使用socks5二级代理
        .proxyInterceptFactory(() -> new HttpProxyIntercept() { //拦截http请求和响应
          @Override
          public boolean beforeRequest(Channel clientChannel, HttpRequest httpRequest) {
            //替换UA，伪装成手机浏览器
            httpRequest.headers().set(HttpHeaderNames.USER_AGENT,"Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1");
            return true;
          }

          @Override
          public boolean afterResponse(Channel clientChannel, Channel proxyChannel,
              HttpResponse httpResponse) {
            //拦截响应，添加一个响应头
            httpResponse.headers().add("intercept", "test");
            return true;
          }

        }).start(9999);*/
  }

}
