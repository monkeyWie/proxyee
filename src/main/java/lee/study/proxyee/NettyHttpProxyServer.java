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
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;

public class NettyHttpProxyServer {

    public static HttpResponseStatus SUCCESS;
    public static SslContext clientSslCtx;
    public static PrivateKey caPriKey;
    public static PublicKey caPubKey;
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
        //CA私钥和公钥用于给动态生成的网站SSL证书签证
        caPriKey = CertUtil.loadPriKey(Thread.currentThread().getContextClassLoader().getResourceAsStream("ca_private.pem"));
        caPubKey = CertUtil.loadPubKey(Thread.currentThread().getContextClassLoader().getResourceAsStream("ca_public.der"));
        //生产一对随机公私钥用于网站SSL证书动态创建
        KeyPair keyPair = CertUtil.genKeyPair();
        serverPriKey = keyPair.getPrivate();
        serverPubKey = keyPair.getPublic();
        proxyGroup = new NioEventLoopGroup();
        if(proxyInterceptFactory==null){
            proxyInterceptFactory = new DefaultInterceptFactory();
        }
    }

    public NettyHttpProxyServer initProxyInterceptFactory(ProxyInterceptFactory proxyInterceptFactory){
        this.proxyInterceptFactory=proxyInterceptFactory;
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
        new  NettyHttpProxyServer().initProxyInterceptFactory(() -> new HttpProxyIntercept() {
            @Override
            public boolean beforeRequest(Channel channel, HttpRequest httpRequest) {
                return false;
            }

            @Override
            public boolean beforeRequest(Channel channel, HttpContent httpContent) {
                return false;
            }

            @Override
            public boolean afterResponse(Channel channel, HttpResponse httpResponse) {
                httpResponse.headers().set("Intercept","111");
                return false;
            }

            @Override
            public boolean afterResponse(Channel channel, HttpContent httpContent) {
                return false;
            }
        }).start(8999);
    }

}
