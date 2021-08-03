package com.github.monkeywie.proxyee.server;

import com.github.monkeywie.proxyee.crt.CertPool;
import com.github.monkeywie.proxyee.crt.CertUtil;
import com.github.monkeywie.proxyee.exception.HttpProxyExceptionHandle;
import com.github.monkeywie.proxyee.handler.HttpProxyServerHandler;
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
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;

public class HttpProxyServer {

    private final static InternalLogger log = InternalLoggerFactory.getInstance(HttpProxyServer.class);

    //http代理隧道握手成功
    public final static HttpResponseStatus SUCCESS = new HttpResponseStatus(200,
            "Connection established");
    public final static HttpResponseStatus UNAUTHORIZED = new HttpResponseStatus(407,
            "Unauthorized");

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
                log.warn("SSL init fail,cause:" + e.getMessage());
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
        start(null, port);
    }

    public void start(String ip, int port) {
        try {
            ChannelFuture channelFuture = doBind(ip, port);
            CountDownLatch latch = new CountDownLatch(1);
            channelFuture.addListener(future -> {
                if (future.cause() != null) {
                    httpProxyExceptionHandle.startCatch(future.cause());
                }
                latch.countDown();
            });
            latch.await();
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            httpProxyExceptionHandle.startCatch(e);
        } finally {
            close();
        }
    }

    public CompletionStage<Void> startAsync(int port) {
        return startAsync(null, port);
    }

    public CompletionStage<Void> startAsync(String ip, int port) {
        ChannelFuture channelFuture = doBind(ip, port);

        CompletableFuture<Void> future = new CompletableFuture<>();
        channelFuture.addListener(start -> {
            if (start.isSuccess()) {
                future.complete(null);
                shutdownHook();
            } else {
                future.completeExceptionally(start.cause());
                close();
            }
        });
        return future;
    }

    private ChannelFuture doBind(String ip, int port) {
        init();
        bossGroup = new NioEventLoopGroup(serverConfig.getBossGroupThreads());
        workerGroup = new NioEventLoopGroup(serverConfig.getWorkerGroupThreads());
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
//                .option(ChannelOption.SO_BACKLOG, 100)
                .handler(new LoggingHandler(LogLevel.DEBUG))
                .childHandler(new ChannelInitializer<Channel>() {

                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast("httpCodec", new HttpServerCodec());
                        ch.pipeline().addLast("serverHandle",
                                new HttpProxyServerHandler(serverConfig, proxyInterceptInitializer, proxyConfig,
                                        httpProxyExceptionHandle));
                    }
                });

        return ip == null ? bootstrap.bind(port) : bootstrap.bind(ip, port);
    }

    /**
     * 释放资源
     */
    public void close() {
        EventLoopGroup eventLoopGroup = serverConfig.getProxyLoopGroup();
        if (!(eventLoopGroup.isShutdown() || eventLoopGroup.isShuttingDown())) {
            eventLoopGroup.shutdownGracefully();
        }
        if (!(bossGroup.isShutdown() || bossGroup.isShuttingDown())) {
            bossGroup.shutdownGracefully();
        }
        if (!(workerGroup.isShutdown() || workerGroup.isShuttingDown())) {
            workerGroup.shutdownGracefully();
        }

        CertPool.clear();
    }

    /**
     * 注册JVM关闭的钩子以释放资源
     */
    public void shutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::close, "Server Shutdown Thread"));
    }

}
