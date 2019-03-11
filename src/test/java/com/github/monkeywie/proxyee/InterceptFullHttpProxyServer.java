package com.github.monkeywie.proxyee;

import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptInitializer;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptPipeline;
import com.github.monkeywie.proxyee.intercept.common.CertDownIntercept;
import com.github.monkeywie.proxyee.intercept.common.FullRequestIntercept;
import com.github.monkeywie.proxyee.intercept.common.FullResponseIntercept;
import com.github.monkeywie.proxyee.server.HttpProxyServer;
import com.github.monkeywie.proxyee.server.HttpProxyServerConfig;
import com.github.monkeywie.proxyee.util.HttpUtil;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.*;

import java.nio.charset.Charset;

public class InterceptFullHttpProxyServer {

  public static void main(String[] args) throws Exception {
    HttpProxyServerConfig config = new HttpProxyServerConfig();
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
                });
                pipeline.addLast(new FullResponseIntercept() {

                  @Override
                  public boolean match(HttpRequest httpRequest, HttpResponse httpResponse, HttpProxyInterceptPipeline pipeline) {
                    //请求体中包含user字符串
                    if(httpRequest instanceof FullHttpRequest){
                      FullHttpRequest fullHttpRequest = (FullHttpRequest) httpRequest;
                      String content = fullHttpRequest.content().toString(Charset.defaultCharset());
                      return content.matches("user");
                    }
                    return false;
                  }

                  @Override
                  public void handelResponse(HttpRequest httpRequest, FullHttpResponse httpResponse, HttpProxyInterceptPipeline pipeline) {
                    //打印原始响应信息
                    System.out.println(httpResponse.toString());
                    System.out.println(httpResponse.content().toString(Charset.defaultCharset()));
                    //修改响应头和响应体
                    httpResponse.headers().set("handel", "edit head");
                    /*int index = ByteUtil.findText(httpResponse.content(), "<head>");
                    ByteUtil.insertText(httpResponse.content(), index, "<script>alert(1)</script>");*/
                    httpResponse.content().writeBytes("<script>alert('hello proxyee')</script>".getBytes());
                  }
                });

              }
            })
            .start(9999);
  }
}
