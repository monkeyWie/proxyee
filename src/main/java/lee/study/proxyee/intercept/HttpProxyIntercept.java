package lee.study.proxyee.intercept;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

public class HttpProxyIntercept {
    /**
     * 拦截代理服务器到目标服务器的请求头
     * @param clientChannel
     * @param httpRequest
     * @return
     */
    public boolean beforeRequest(Channel clientChannel, HttpRequest httpRequest){
        return true;
    }

    /**
     * 拦截代理服务器到目标服务器的请求体
     * @param clientChannel
     * @param httpContent
     * @return
     */
    public boolean beforeRequest(Channel clientChannel, HttpContent httpContent){
        return true;
    }

    /**
     * 拦截代理服务器到客户端的响应头
     * @param clientChannel
     * @param proxyChannel
     * @param httpResponse
     * @return
     */
    public boolean afterResponse(Channel clientChannel, Channel proxyChannel, HttpResponse httpResponse){
        return true;
    }

    /**
     * 拦截代理服务器到客户端的响应体
     * @param clientChannel
     * @param proxyChannel
     * @param httpContent
     * @return
     */
    public boolean afterResponse(Channel clientChannel, Channel proxyChannel, HttpContent httpContent){
        return true;
    }
}
