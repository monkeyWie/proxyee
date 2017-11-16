package lee.study;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import lee.study.proxyee.handler.HttpProxyServerHandle;

public class TestClose {

  public static void main(String[] args) {
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    try {
      ServerBootstrap b = new ServerBootstrap();
      b.group(bossGroup, workerGroup)
          .channel(NioServerSocketChannel.class)
          .option(ChannelOption.SO_BACKLOG, 100)
          .option(ChannelOption.TCP_NODELAY, true)
//                    .handler(new LoggingHandler(LogLevel.ERROR))
          .childHandler(new ChannelInitializer<Channel>() {

            @Override
            protected void initChannel(Channel ch) throws Exception {
              ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                  System.out.println(msg.toString());
                }

                @Override
                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
                    throws Exception {
                  super.exceptionCaught(ctx, cause);
                  System.out.println("exceptionCaught");
                }

                @Override
                public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
                  super.channelUnregistered(ctx);
                  System.out.println(111111);
                }
              });
            }
          });
      ChannelFuture f = b
          .bind(8899)
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
