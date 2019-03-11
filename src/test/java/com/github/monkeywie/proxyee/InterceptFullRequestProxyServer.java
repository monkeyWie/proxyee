package com.github.monkeywie.proxyee;

import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptInitializer;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptPipeline;
import com.github.monkeywie.proxyee.intercept.common.CertDownIntercept;
import com.github.monkeywie.proxyee.intercept.common.FullRequestIntercept;
import com.github.monkeywie.proxyee.intercept.common.FullResponseIntercept;
import com.github.monkeywie.proxyee.server.HttpProxyServer;
import com.github.monkeywie.proxyee.server.HttpProxyServerConfig;
import com.github.monkeywie.proxyee.util.HttpUtil;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;

import java.nio.charset.Charset;


public class InterceptFullRequestProxyServer {

  /*
    curl -x 127.0.0.1:9999 \
    -X POST \
    http://www.baidu.com \
    -H 'Content-Type: application/json' \
    -d '{"name":"admin","pwd":"123456"}'

    echo '{"name":"admin","pwd":"123456"}' | gzip | \
        curl -x 127.0.0.1:9999 \
        http://www.baidu.com \
        -H "Content-Encoding: gzip" \
        -H "Content-Type: application/json" \
        --data-binary @-
   */
  public static void main(String[] args) throws Exception {
    HttpProxyServerConfig config =  new HttpProxyServerConfig();
    config.setHandleSsl(true);
    new HttpProxyServer()
        .serverConfig(config)
        .proxyInterceptInitializer(new HttpProxyInterceptInitializer() {
          @Override
          public void init(HttpProxyInterceptPipeline pipeline) {
            pipeline.addLast(new CertDownIntercept());
            pipeline.addLast(new FullRequestIntercept() {

              @Override
              public boolean match(HttpRequest httpRequest, HttpProxyInterceptPipeline pipeline) {
                //如果是json报文
                if(HttpUtil.checkHeader(httpRequest.headers(), HttpHeaderNames.CONTENT_TYPE,"^(?i)application/json.*$")){
                  return true;
                }
                return false;
              }

              @Override
              public void handelRequest(FullHttpRequest httpRequest, HttpProxyInterceptPipeline pipeline) {
                ByteBuf content = httpRequest.content();
                //打印请求信息
                System.out.println(httpRequest.toString());
                System.out.println(content.toString(Charset.defaultCharset()));
                //修改请求体
                String body = "{\"name\":\"intercept\",\"pwd\":\"123456\"}";
                content.clear();
                content.writeBytes(body.getBytes());
              }

            });
          }
        })
        .start(9999);
  }
}
