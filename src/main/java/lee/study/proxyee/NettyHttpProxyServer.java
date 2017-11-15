package lee.study.proxyee;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lee.study.proxyee.crt.CertUtil;
import lee.study.proxyee.handler.HttpProxyServerHandle;
import lee.study.proxyee.intercept.DefaultInterceptFactory;
import lee.study.proxyee.intercept.HttpProxyIntercept;
import lee.study.proxyee.intercept.ProxyInterceptFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;

public class NettyHttpProxyServer {

    public static HttpResponseStatus SUCCESS;
    public static SslContext clientSslCtx;
    public static String issuer;
    public static PrivateKey caPriKey;
    public static PrivateKey serverPriKey;
    public static PublicKey serverPubKey;
    public static EventLoopGroup proxyGroup;

    private ProxyInterceptFactory proxyInterceptFactory;

    private void init() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        Method method = HttpResponseStatus.class.getDeclaredMethod("newStatus", int.class, String.class);
        method.setAccessible(true);
        SUCCESS = (HttpResponseStatus) method.invoke(null, 200, "Connection established");
        clientSslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
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

    public NettyHttpProxyServer initProxyInterceptFactory(ProxyInterceptFactory proxyInterceptFactory) {
        this.proxyInterceptFactory = proxyInterceptFactory;
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
//                    .option(ChannelOption.SO_BACKLOG, 100)
                    .option(ChannelOption.TCP_NODELAY, true)
//                    .handler(new LoggingHandler(LogLevel.ERROR))
                    .childHandler(new ChannelInitializer<Channel>() {

                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ch.pipeline().addLast("httpCodec", new HttpServerCodec());
                            ch.pipeline().addLast("serverHandle", new HttpProxyServerHandle(proxyInterceptFactory.build()));
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
//        new NettyHttpProxyServer().start(9999);
        new NettyHttpProxyServer().initProxyInterceptFactory(() -> new HttpProxyIntercept() {

            @Override
            public boolean afterResponse(Channel clientChannel, Channel proxyChannel, HttpResponse httpResponse) {
                //拦截响应，添加一个响应头
                httpResponse.headers().add("intercept","test");
                return true;
            }

        }).start(9999);
    }

}
