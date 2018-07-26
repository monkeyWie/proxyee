package com.github.monkeywie.proxyee;

import com.github.monkeywie.proxyee.exception.HttpProxyExceptionHandle;
import com.github.monkeywie.proxyee.intercept.HttpProxyIntercept;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptInitializer;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptPipeline;
import com.github.monkeywie.proxyee.intercept.common.CertDownIntercept;
import com.github.monkeywie.proxyee.server.HttpProxyServer;
import com.github.monkeywie.proxyee.server.HttpProxyServerConfig;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

public class InterceptHttpProxyServer {

  public static void main(String[] args) throws Exception {
    HttpProxyServerConfig config =  new HttpProxyServerConfig();
    config.setHandleSsl(true);
    new HttpProxyServer()
        .serverConfig(config)
//        .proxyConfig(new ProxyConfig(ProxyType.SOCKS5, "127.0.0.1", 1085))  //使用socks5二级代理
        .proxyInterceptInitializer(new HttpProxyInterceptInitializer() {
          @Override
          public void init(HttpProxyInterceptPipeline pipeline) {
            pipeline.addLast(new CertDownIntercept());  //处理证书下载
            pipeline.addLast(new HttpProxyIntercept() {
              @Override
              public void beforeRequest(Channel clientChannel, HttpRequest httpRequest,
                  HttpProxyInterceptPipeline pipeline) throws Exception {
                //替换UA，伪装成手机浏览器
                /*httpRequest.headers().set(HttpHeaderNames.USER_AGENT,
                    "Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1");*/
                //转到下一个拦截器处理
                pipeline.beforeRequest(clientChannel, httpRequest);
              }

              @Override
              public void afterResponse(Channel clientChannel, Channel proxyChannel,
                  HttpResponse httpResponse, HttpProxyInterceptPipeline pipeline) throws Exception {

                //拦截响应，添加一个响应头
                httpResponse.headers().add("intercept", "test");
                //转到下一个拦截器处理
                pipeline.afterResponse(clientChannel, proxyChannel, httpResponse);
              }
            });
          }
        })
        .httpProxyExceptionHandle(new HttpProxyExceptionHandle() {
          @Override
          public void beforeCatch(Channel clientChannel, Throwable cause) throws Exception {
            cause.printStackTrace();
          }

          @Override
          public void afterCatch(Channel clientChannel, Channel proxyChannel, Throwable cause)
              throws Exception {
            cause.printStackTrace();
          }
        })
        .start(9999);
  }
}
