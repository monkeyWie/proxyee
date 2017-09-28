package lee.study.proxyee.handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import lee.study.proxyee.NettyHttpProxyServer;
import lee.study.proxyee.crt.CertPool;
import lee.study.proxyee.intercept.HttpProxyIntercept;
import lee.study.proxyee.util.HttpUtil;

public class HttpProxyServerHandle extends ChannelInboundHandlerAdapter {

    private ChannelFuture cf;
    private String host;
    private int port;
    private boolean isSSL = false;
    private int status = 0;
    private HttpProxyIntercept httpProxyHook;

    public HttpProxyServerHandle(HttpProxyIntercept httpProxyHook) {
        this.httpProxyHook = httpProxyHook;
    }

    public HttpProxyIntercept getHttpProxyHook() {
        return httpProxyHook;
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            //第一次建立连接取host和端口号和处理代理握手
            if (status == 0) {
                status = 1;
                HttpUtil.RequestProto requestProto = HttpUtil.getRequestProto(request);
                this.host = requestProto.getHost();
                this.port = requestProto.getPort();
                if ("CONNECT".equalsIgnoreCase(request.method().name())) {//建立代理握手
                    status = 2;
                    HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, NettyHttpProxyServer.SUCCESS);
                    ctx.writeAndFlush(response);
                    ctx.channel().pipeline().remove("httpCodec");
                    return;
                }
            }
            if(httpProxyHook.beforeRequest(ctx.channel(),request)){
                return;
            }
            handleProxyData(ctx, msg);
        } else if (msg instanceof HttpContent) {
            if (status != 2) {
                if(httpProxyHook.beforeRequest(ctx.channel(),(HttpContent) msg)){
                    return;
                }
                handleProxyData(ctx, msg);
            } else {
                status = 1;
            }
        } else { //ssl和websocket的握手处理
            ByteBuf byteBuf = (ByteBuf) msg;
            if (byteBuf.getByte(0) == 22) {//ssl握手
                isSSL = true;
                SslContext sslCtx = SslContextBuilder.forServer(NettyHttpProxyServer.serverPriKey, CertPool.getCert(this.host)).build();
                ctx.pipeline().addFirst("httpCodec", new HttpServerCodec());
                ctx.pipeline().addFirst("sslHandle", sslCtx.newHandler(ctx.alloc()));
                //重新过一遍pipeline，拿到解密后的的http报文
                ctx.pipeline().fireChannelRead(msg);
                return;
            }
            transData(ctx, msg);
        }
    }

    private void handleProxyData(final ChannelHandlerContext ctx, final Object msg) throws InterruptedException {
        if (cf == null) {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(NettyHttpProxyServer.proxyGroup) // 注册线程池
                    .channel(NioSocketChannel.class) // 使用NioSocketChannel来作为连接用的channel类
                    .handler(new HttpProxyInitializer(ctx.channel(), isSSL));
            cf = bootstrap.connect(host, port).sync();
            /*cf.addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture future) throws Exception {
                    System.out.println("11111"+msg);
                    if (future.isSuccess()) {
                        future.channel().writeAndFlush(msg);
                    } else {
                        ctx.channel().close();
                    }
                }
            });*/
        }
        cf.channel().writeAndFlush(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if(cf!=null){
            cf.channel().closeFuture().sync();
        }
        super.exceptionCaught(ctx, cause);
    }

    private void transData(final ChannelHandlerContext ctx, final Object msg) throws InterruptedException {
        if (cf == null) {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(NettyHttpProxyServer.proxyGroup) // 注册线程池
                    .channel(NioSocketChannel.class) // 使用NioSocketChannel来作为连接用的channel类
                    .handler(new ChannelInitializer() {

                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelRead(ChannelHandlerContext ctx0, Object msg0) throws Exception {
                                    ctx.channel().writeAndFlush(msg0);
                                }
                            });
                        }
                    });
            cf = bootstrap.connect(host, port).sync();
        }
        cf.channel().writeAndFlush(msg);
    }

    public static void main(String[] args) {
        System.out.println((char) 22);
    }

}
