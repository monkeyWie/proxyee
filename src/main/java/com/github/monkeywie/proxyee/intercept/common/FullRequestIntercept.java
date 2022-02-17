package com.github.monkeywie.proxyee.intercept.common;

import com.github.monkeywie.proxyee.intercept.HttpProxyIntercept;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptPipeline;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.*;

public abstract class FullRequestIntercept extends HttpProxyIntercept {

    /**
     * default max content length size is 8MB
     */
    private static final int DEFAULT_MAX_CONTENT_LENGTH = 1024 * 1024 * 8;

    private int maxContentLength;

    public FullRequestIntercept() {
        this(DEFAULT_MAX_CONTENT_LENGTH);
    }

    public FullRequestIntercept(int maxContentLength) {
        this.maxContentLength = maxContentLength;
    }


    @Override
    public final void beforeRequest(Channel clientChannel, HttpRequest httpRequest, HttpProxyInterceptPipeline pipeline) throws Exception {
        if (httpRequest instanceof FullHttpRequest) {
            FullHttpRequest fullHttpRequest = (FullHttpRequest) httpRequest;
            handleRequest(fullHttpRequest, pipeline);
            fullHttpRequest.content().markReaderIndex();
            fullHttpRequest.content().retain();
            if (fullHttpRequest.headers().contains(HttpHeaderNames.CONTENT_LENGTH)) {
                fullHttpRequest.headers().set(HttpHeaderNames.CONTENT_LENGTH, fullHttpRequest.content().readableBytes());
            }
        } else if (match(httpRequest, pipeline)) {
            // 重置拦截器
            // reset interceptor
            pipeline.resetBeforeHead();
            // 添加gzip解压处理
            // Add gzip decompression processing
            clientChannel.pipeline().addAfter("httpCodec", "decompress", new HttpContentDecompressor());
            // 添加Full request解码器
            // Add Full request decoder
            clientChannel.pipeline().addAfter("decompress", "aggregator", new HttpObjectAggregator(maxContentLength));
            // 重新过一遍处理器链
            // Go through the processor chain all over again
            clientChannel.pipeline().fireChannelRead(httpRequest);
            return;
        }
        pipeline.beforeRequest(clientChannel, httpRequest);
    }

    @Override
    public void afterResponse(Channel clientChannel, Channel proxyChannel, HttpResponse httpResponse, HttpProxyInterceptPipeline pipeline) throws Exception {
        // 如果是FullHttpRequest
        // If it is FullHttpRequest
        if (pipeline.getHttpRequest() instanceof FullHttpRequest) {
            if (clientChannel.pipeline().get("decompress") != null) {
                clientChannel.pipeline().remove("decompress");
            }
            if (clientChannel.pipeline().get("aggregator") != null) {
                clientChannel.pipeline().remove("aggregator");
            }
            FullHttpRequest httpRequest = (FullHttpRequest) pipeline.getHttpRequest();
            httpRequest.content().resetReaderIndex();
        }
        pipeline.afterResponse(clientChannel, proxyChannel, httpResponse);
    }

    /**
     * <div class="zh">匹配到的请求会解码成FullRequest</div>
     * <div class="en">The matched request will be decoded into FullRequest</div>
     */
    public abstract boolean match(HttpRequest httpRequest, HttpProxyInterceptPipeline pipeline);

    /**
     * <div class="zh">拦截并处理响应</div>
     * <div class="en">Intercept and process the response</div>
     */
    public void handleRequest(FullHttpRequest httpRequest, HttpProxyInterceptPipeline pipeline) {
    }
}
