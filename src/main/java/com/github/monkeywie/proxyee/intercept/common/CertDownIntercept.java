package com.github.monkeywie.proxyee.intercept.common;

import com.github.monkeywie.proxyee.crt.CertUtil;
import com.github.monkeywie.proxyee.intercept.HttpProxyIntercept;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptPipeline;
import com.github.monkeywie.proxyee.util.ProtoUtil;
import com.github.monkeywie.proxyee.util.ProtoUtil.RequestProto;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import java.net.InetSocketAddress;

/**
 * 处理证书下载页面 http://proxyServerIp:proxyServerPort
 */
public class CertDownIntercept extends HttpProxyIntercept {

  private boolean crtFlag = false;

  @Override
  public void beforeRequest(Channel clientChannel, HttpRequest httpRequest,
      HttpProxyInterceptPipeline pipeline) throws Exception {
    RequestProto requestProto = ProtoUtil.getRequestProto(httpRequest);
    if (requestProto == null) { //bad request
      clientChannel.close();
      return;
    }
    InetSocketAddress inetSocketAddress = (InetSocketAddress) clientChannel.localAddress();
    if (requestProto.getHost().equals(inetSocketAddress.getHostString()) &&
        requestProto.getPort() == inetSocketAddress.getPort()) {
      crtFlag = true;
      if (httpRequest.uri().matches("^.*/ca.crt.*$")) {  //下载证书
        HttpResponse httpResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
            HttpResponseStatus.OK);
        byte[] bts = CertUtil
            .loadCert(Thread.currentThread().getContextClassLoader().getResourceAsStream("ca.crt"))
            .getEncoded();
        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/x-x509-ca-cert");
        httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, bts.length);
        httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        HttpContent httpContent = new DefaultLastHttpContent();
        httpContent.content().writeBytes(bts);
        clientChannel.writeAndFlush(httpResponse);
        clientChannel.writeAndFlush(httpContent);
        clientChannel.close();
      } else if (httpRequest.uri().matches("^.*/favicon.ico$")) {
        clientChannel.close();
      } else {  //跳转下载页面
        HttpResponse httpResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
            HttpResponseStatus.OK);
        String html = "<html><body><div style=\"margin-top:100px;text-align:center;\"><a href=\"ca.crt\">ProxyeeRoot ca.crt</a></div></body></html>";
        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html;charset=utf-8");
        httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, html.getBytes().length);
        httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        HttpContent httpContent = new DefaultLastHttpContent();
        httpContent.content().writeBytes(html.getBytes());
        clientChannel.writeAndFlush(httpResponse);
        clientChannel.writeAndFlush(httpContent);
      }
    } else {
      pipeline.beforeRequest(clientChannel, httpRequest);
    }
  }

  @Override
  public void beforeRequest(Channel clientChannel, HttpContent httpContent,
      HttpProxyInterceptPipeline pipeline) throws Exception {
    if (!crtFlag) {
      pipeline.beforeRequest(clientChannel, httpContent);
    }
  }
}
