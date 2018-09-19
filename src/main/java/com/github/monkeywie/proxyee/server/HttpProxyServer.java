package com.github.monkeywie.proxyee.server;

import com.github.monkeywie.proxyee.crt.CertPool;
import com.github.monkeywie.proxyee.crt.CertUtil;
import com.github.monkeywie.proxyee.exception.HttpProxyExceptionHandle;
import com.github.monkeywie.proxyee.handler.HttpProxyServerHandle;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptInitializer;
import com.github.monkeywie.proxyee.proxy.ProxyConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public class HttpProxyServer {

  //http代理隧道握手成功
  public final static HttpResponseStatus SUCCESS = new HttpResponseStatus(200,
      "Connection established");

  private HttpProxyCACertFactory caCertFactory;
  private HttpProxyServerConfig serverConfig;
  private HttpProxyInterceptInitializer proxyInterceptInitializer;
  private HttpProxyExceptionHandle httpProxyExceptionHandle;
  private ProxyConfig proxyConfig;

  private EventLoopGroup bossGroup;
  private EventLoopGroup workerGroup;

  private void init() {
    if (serverConfig == null) {
      serverConfig = new HttpProxyServerConfig();
    }
    serverConfig.setProxyLoopGroup(new NioEventLoopGroup(serverConfig.getProxyGroupThreads()));

    if (serverConfig.isHandleSsl()) {
      try {
        serverConfig.setClientSslCtx(
            SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build());
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        X509Certificate caCert;
        PrivateKey caPriKey;
        if (caCertFactory == null) {
          caCert = CertUtil.loadCert(classLoader.getResourceAsStream("ca.crt"));
          caPriKey = CertUtil.loadPriKey(classLoader.getResourceAsStream("ca_private.der"));
        } else {
          caCert = caCertFactory.getCACert();
          caPriKey = caCertFactory.getCAPriKey();
        }
        //读取CA证书使用者信息
        serverConfig.setIssuer(CertUtil.getSubject(caCert));
        //读取CA证书有效时段(server证书有效期超出CA证书的，在手机上会提示证书不安全)
        serverConfig.setCaNotBefore(caCert.getNotBefore());
        serverConfig.setCaNotAfter(caCert.getNotAfter());
        //CA私钥用于给动态生成的网站SSL证书签证
        serverConfig.setCaPriKey(caPriKey);
        //生产一对随机公私钥用于网站SSL证书动态创建
        KeyPair keyPair = CertUtil.genKeyPair();
        serverConfig.setServerPriKey(keyPair.getPrivate());
        serverConfig.setServerPubKey(keyPair.getPublic());
      } catch (Exception e) {
        serverConfig.setHandleSsl(false);
      }
    }
    if (proxyInterceptInitializer == null) {
      proxyInterceptInitializer = new HttpProxyInterceptInitializer();
    }
    if (httpProxyExceptionHandle == null) {
      httpProxyExceptionHandle = new HttpProxyExceptionHandle();
    }
  }

  public HttpProxyServer serverConfig(HttpProxyServerConfig serverConfig) {
    this.serverConfig = serverConfig;
    return this;
  }

  public HttpProxyServer proxyInterceptInitializer(
      HttpProxyInterceptInitializer proxyInterceptInitializer) {
    this.proxyInterceptInitializer = proxyInterceptInitializer;
    return this;
  }

  public HttpProxyServer httpProxyExceptionHandle(
      HttpProxyExceptionHandle httpProxyExceptionHandle) {
    this.httpProxyExceptionHandle = httpProxyExceptionHandle;
    return this;
  }

  public HttpProxyServer proxyConfig(ProxyConfig proxyConfig) {
    this.proxyConfig = proxyConfig;
    return this;
  }

  public HttpProxyServer caCertFactory(HttpProxyCACertFactory caCertFactory) {
    this.caCertFactory = caCertFactory;
    return this;
  }

  public void start(int port) {
    init();
    bossGroup = new NioEventLoopGroup(serverConfig.getBossGroupThreads());
    workerGroup = new NioEventLoopGroup(serverConfig.getWorkerGroupThreads());
    try {
      ServerBootstrap b = new ServerBootstrap();
      b.group(bossGroup, workerGroup)
          .channel(NioServerSocketChannel.class)
//          .option(ChannelOption.SO_BACKLOG, 100)
          .handler(new LoggingHandler(LogLevel.DEBUG))
          .childHandler(new ChannelInitializer<Channel>() {

            @Override
            protected void initChannel(Channel ch) throws Exception {
              ch.pipeline().addLast("httpCodec", new HttpServerCodec());
              ch.pipeline().addLast("serverHandle",
                  new HttpProxyServerHandle(serverConfig, proxyInterceptInitializer, proxyConfig,
                      httpProxyExceptionHandle));
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

  public void close() {
    serverConfig.getProxyLoopGroup().shutdownGracefully();
    bossGroup.shutdownGracefully();
    workerGroup.shutdownGracefully();
    CertPool.clear();
  }

}
