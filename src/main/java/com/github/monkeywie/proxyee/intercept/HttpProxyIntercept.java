package com.github.monkeywie.proxyee.intercept;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

/**
 * <div class="zh">http拦截器</div>
 * <div class="en">http interceptor</div>
 * beforeForward -> beforeRequest -> afterResponse
 */
public class HttpProxyIntercept {

    /**
     * <div class="zh">在与目标服务器建立连接之前拦截</div>
     * <div class="en">Intercept before establishing a connection with the target server</div>
     */
    public void beforeConnect(Channel clientChannel, HttpProxyInterceptPipeline pipeline) throws Exception {
    }

    /**
     * <div class="zh">拦截代理服务器到目标服务器的请求头</div>
     * <div class="en">Intercept the request header from the proxy server to the target server</div>
     */
    public void beforeRequest(Channel clientChannel, HttpRequest httpRequest,
                              HttpProxyInterceptPipeline pipeline) throws Exception {
        pipeline.beforeRequest(clientChannel, httpRequest);
    }

    /**
     * <div class="zh">拦截代理服务器到目标服务器的请求体</div>
     * <div class="en">Intercept the request body from the proxy server to the target server</div>
     */
    public void beforeRequest(Channel clientChannel, HttpContent httpContent,
                              HttpProxyInterceptPipeline pipeline) throws Exception {
        pipeline.beforeRequest(clientChannel, httpContent);
    }

    /**
     * <div class="zh">拦截代理服务器到客户端的响应头</div>
     * <div class="en">Intercept the response headers from the proxy server to the client</div>
     */
    public void afterResponse(Channel clientChannel, Channel proxyChannel, HttpResponse httpResponse,
                              HttpProxyInterceptPipeline pipeline) throws Exception {
        pipeline.afterResponse(clientChannel, proxyChannel, httpResponse);
    }


    /**
     * <div class="zh">拦截代理服务器到客户端的响应体</div>
     * <div class="en">Intercept the response body from the proxy server to the client</div>
     */
    public void afterResponse(Channel clientChannel, Channel proxyChannel, HttpContent httpContent,
                              HttpProxyInterceptPipeline pipeline)
            throws Exception {
        pipeline.afterResponse(clientChannel, proxyChannel, httpContent);
    }
}
