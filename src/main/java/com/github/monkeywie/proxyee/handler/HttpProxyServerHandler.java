package com.github.monkeywie.proxyee.handler;

import com.github.monkeywie.proxyee.crt.CertPool;
import com.github.monkeywie.proxyee.exception.HttpProxyExceptionHandle;
import com.github.monkeywie.proxyee.intercept.HttpProxyIntercept;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptInitializer;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptPipeline;
import com.github.monkeywie.proxyee.proxy.ProxyConfig;
import com.github.monkeywie.proxyee.proxy.ProxyHandleFactory;
import com.github.monkeywie.proxyee.server.HttpProxyServer;
import com.github.monkeywie.proxyee.server.HttpProxyServerConfig;
import com.github.monkeywie.proxyee.server.auth.HttpAuthContext;
import com.github.monkeywie.proxyee.server.auth.HttpProxyAuthenticationProvider;
import com.github.monkeywie.proxyee.server.auth.model.HttpToken;
import com.github.monkeywie.proxyee.util.ProtoUtil;
import com.github.monkeywie.proxyee.util.ProtoUtil.RequestProto;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.resolver.NoopAddressResolverGroup;
import io.netty.util.ReferenceCountUtil;

import java.net.InetSocketAddress;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

public class HttpProxyServerHandler extends ChannelInboundHandlerAdapter {

    private ChannelFuture cf;
    private RequestProto requestProto;
    private int status = 0;
    private final HttpProxyServerConfig serverConfig;
    private final ProxyConfig proxyConfig;
    private final HttpProxyInterceptInitializer interceptInitializer;
    private HttpProxyInterceptPipeline interceptPipeline;
    private final HttpProxyExceptionHandle exceptionHandle;
    private List requestList;
    private boolean isConnect;

    public HttpProxyServerConfig getServerConfig() {
        return serverConfig;
    }

    public HttpProxyInterceptPipeline getInterceptPipeline() {
        return interceptPipeline;
    }

    public HttpProxyExceptionHandle getExceptionHandle() {
        return exceptionHandle;
    }

    public HttpProxyServerHandler(HttpProxyServerConfig serverConfig, HttpProxyInterceptInitializer interceptInitializer, ProxyConfig proxyConfig, HttpProxyExceptionHandle exceptionHandle) {
        this.serverConfig = serverConfig;
        this.proxyConfig = proxyConfig;
        this.interceptInitializer = interceptInitializer;
        this.exceptionHandle = exceptionHandle;
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            // 第一次建立连接取host和端口号和处理代理握手
            if (status == 0) {
                requestProto = ProtoUtil.getRequestProto(request);
                if (requestProto == null) { // bad request
                    ctx.channel().close();
                    return;
                }
                // 首次连接处理
                if (serverConfig.getHttpProxyAcceptHandler() != null
                        && !serverConfig.getHttpProxyAcceptHandler().onAccept(request, ctx.channel())) {
                    status = 2;
                    ctx.channel().close();
                    return;
                }
                // 代理身份验证
                if (!authenticate(ctx, request)) {
                    status = 2;
                    ctx.channel().close();
                    return;
                }
                status = 1;
                if ("CONNECT".equalsIgnoreCase(request.method().name())) {// 建立代理握手
                    status = 2;
                    HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpProxyServer.SUCCESS);
                    ctx.writeAndFlush(response);
                    ctx.channel().pipeline().remove("httpCodec");
                    // fix issue #42
                    ReferenceCountUtil.release(msg);
                    return;
                }
            }
            interceptPipeline = buildPipeline();
            interceptPipeline.setRequestProto(requestProto.copy());
            // fix issue #27
            if (request.uri().indexOf("/") != 0) {
                URL url = new URL(request.uri());
                request.setUri(url.getFile());
            }
            interceptPipeline.beforeRequest(ctx.channel(), request);
        } else if (msg instanceof HttpContent) {
            if (status != 2) {
                interceptPipeline.beforeRequest(ctx.channel(), (HttpContent) msg);
            } else {
                ReferenceCountUtil.release(msg);
                status = 1;
            }
        } else { // ssl和websocket的握手处理
            if (serverConfig.isHandleSsl()) {
                ByteBuf byteBuf = (ByteBuf) msg;
                if (byteBuf.getByte(0) == 22) {// ssl握手
                    requestProto.setSsl(true);
                    int port = ((InetSocketAddress) ctx.channel().localAddress()).getPort();
                    SslContext sslCtx = SslContextBuilder
                            .forServer(serverConfig.getServerPriKey(), CertPool.getCert(port, requestProto.getHost(), serverConfig)).build();
                    ctx.pipeline().addFirst("httpCodec", new HttpServerCodec());
                    ctx.pipeline().addFirst("sslHandle", sslCtx.newHandler(ctx.alloc()));
                    // 重新过一遍pipeline，拿到解密后的的http报文
                    ctx.pipeline().fireChannelRead(msg);
                    return;
                }
            }
            handleProxyData(ctx.channel(), msg, false);
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        if (cf != null) {
            cf.channel().close();
        }
        ctx.channel().close();
        if (serverConfig.getHttpProxyAcceptHandler() != null) {
            serverConfig.getHttpProxyAcceptHandler().onClose(ctx.channel());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cf != null) {
            cf.channel().close();
        }
        ctx.channel().close();
        exceptionHandle.beforeCatch(ctx.channel(), cause);
    }

    private boolean authenticate(ChannelHandlerContext ctx, HttpRequest request) {
        if (serverConfig.getAuthenticationProvider() != null) {
            HttpProxyAuthenticationProvider authProvider = serverConfig.getAuthenticationProvider();
            HttpToken httpToken = authProvider.authenticate(request.headers().get(HttpHeaderNames.PROXY_AUTHORIZATION));
            if (httpToken == null) {
                HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpProxyServer.UNAUTHORIZED);
                response.headers().set(HttpHeaderNames.PROXY_AUTHENTICATE, authProvider.authType() + " realm=\"" + authProvider.authRealm() + "\"");
                ctx.writeAndFlush(response);
                return false;
            }
            HttpAuthContext.setToken(ctx.channel(), httpToken);
        }
        return true;
    }

    private void handleProxyData(Channel channel, Object msg, boolean isHttp) throws Exception {
        if (cf == null) {
            // connection异常 还有HttpContent进来，不转发
            if (isHttp && !(msg instanceof HttpRequest)) {
                return;
            }
            if (interceptPipeline == null) {
                interceptPipeline = buildOnlyConnectPipeline();
                interceptPipeline.setRequestProto(requestProto.copy());
            }
            interceptPipeline.beforeConnect(channel);

            // by default we use the proxy config set in the pipeline
            ProxyHandler proxyHandler = ProxyHandleFactory.build(interceptPipeline.getProxyConfig() == null ?
                    proxyConfig : interceptPipeline.getProxyConfig());

            RequestProto requestProto = interceptPipeline.getRequestProto();
            if (isHttp) {
                HttpRequest httpRequest = (HttpRequest) msg;
                // 检查requestProto是否有修改
                RequestProto newRP = ProtoUtil.getRequestProto(httpRequest);
                if (!newRP.equals(requestProto)) {
                    // 更新Host请求头
                    if ((requestProto.getSsl() && requestProto.getPort() == 443)
                            || (!requestProto.getSsl() && requestProto.getPort() == 80)) {
                        httpRequest.headers().set(HttpHeaderNames.HOST, requestProto.getHost());
                    } else {
                        httpRequest.headers().set(HttpHeaderNames.HOST, requestProto.getHost() + ":" + requestProto.getPort());
                    }
                }
            }

            /*
             * 添加SSL client hello的Server Name Indication extension(SNI扩展) 有些服务器对于client
             * hello不带SNI扩展时会直接返回Received fatal alert: handshake_failure(握手错误)
             * 例如：https://cdn.mdn.mozilla.net/static/img/favicon32.7f3da72dcea1.png
             */
            ChannelInitializer channelInitializer = isHttp ? new HttpProxyInitializer(channel, requestProto, proxyHandler)
                    : new TunnelProxyInitializer(channel, proxyHandler);
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(serverConfig.getProxyLoopGroup()) // 注册线程池
                    .channel(NioSocketChannel.class) // 使用NioSocketChannel来作为连接用的channel类
                    .handler(channelInitializer);
            if (proxyHandler != null) {
                // 代理服务器解析DNS和连接
                bootstrap.resolver(NoopAddressResolverGroup.INSTANCE);
            } else {
                bootstrap.resolver(serverConfig.resolver());
            }
            requestList = new LinkedList();
            cf = bootstrap.connect(requestProto.getHost(), requestProto.getPort());
            cf.addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    future.channel().writeAndFlush(msg);
                    synchronized (requestList) {
                        requestList.forEach(obj -> future.channel().writeAndFlush(obj));
                        requestList.clear();
                        isConnect = true;
                    }
                } else {
                    requestList.forEach(obj -> ReferenceCountUtil.release(obj));
                    requestList.clear();
                    getExceptionHandle().beforeCatch(channel, future.cause());
                    future.channel().close();
                    channel.close();
                }
            });
        } else {
            synchronized (requestList) {
                if (isConnect) {
                    cf.channel().writeAndFlush(msg);
                } else {
                    requestList.add(msg);
                }
            }
        }
    }

    private HttpProxyInterceptPipeline buildPipeline() {
        HttpProxyInterceptPipeline interceptPipeline = new HttpProxyInterceptPipeline(new HttpProxyIntercept() {
            @Override
            public void beforeRequest(Channel clientChannel, HttpRequest httpRequest, HttpProxyInterceptPipeline pipeline)
                    throws Exception {
                handleProxyData(clientChannel, httpRequest, true);
            }

            @Override
            public void beforeRequest(Channel clientChannel, HttpContent httpContent, HttpProxyInterceptPipeline pipeline)
                    throws Exception {
                handleProxyData(clientChannel, httpContent, true);
            }

            @Override
            public void afterResponse(Channel clientChannel, Channel proxyChannel, HttpResponse httpResponse,
                                      HttpProxyInterceptPipeline pipeline) throws Exception {
                clientChannel.writeAndFlush(httpResponse);
                if (HttpHeaderValues.WEBSOCKET.toString().equals(httpResponse.headers().get(HttpHeaderNames.UPGRADE))) {
                    // websocket转发原始报文
                    proxyChannel.pipeline().remove("httpCodec");
                    clientChannel.pipeline().remove("httpCodec");
                }
            }

            @Override
            public void afterResponse(Channel clientChannel, Channel proxyChannel, HttpContent httpContent,
                                      HttpProxyInterceptPipeline pipeline) throws Exception {
                clientChannel.writeAndFlush(httpContent);
            }
        });
        interceptInitializer.init(interceptPipeline);
        return interceptPipeline;
    }

    // fix issue #186: 不拦截https报文时，暴露一个扩展点用于代理设置，并且保持一致的编程接口
    private HttpProxyInterceptPipeline buildOnlyConnectPipeline() {
        HttpProxyInterceptPipeline interceptPipeline = new HttpProxyInterceptPipeline(new HttpProxyIntercept());
        interceptInitializer.init(interceptPipeline);
        return interceptPipeline;
    }
}
