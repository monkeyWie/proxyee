package lee.study;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lee.study.handler.HttpProxyServerHandle;

import java.lang.reflect.Method;

public class NettyHttpProxyServer {

    public static HttpResponseStatus SUCCUSS;

    static {
        try {
            Method method = HttpResponseStatus.class.getDeclaredMethod("newStatus",int.class,String .class);
            method.setAccessible(true);
            SUCCUSS = (HttpResponseStatus) method.invoke(null,200,"Connection established");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start(int port) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup(2);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<Channel>() {

                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ch.pipeline().addLast("httpCodec",new HttpServerCodec());
                            ch.pipeline().addLast("httpObject",new HttpObjectAggregator(65536));
                            ch.pipeline().addLast("serverHandle",new HttpProxyServerHandle());
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

    public static void main(String[] args) {
        new NettyHttpProxyServer().start(8999);
    }
}
