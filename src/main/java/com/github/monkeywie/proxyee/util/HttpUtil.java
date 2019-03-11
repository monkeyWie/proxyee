package com.github.monkeywie.proxyee.util;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.AsciiString;

public class HttpUtil {

  /**
   * 检测url是否匹配
   */
  public static boolean checkUrl(HttpRequest httpRequest, String regex) {
    String host = httpRequest.headers().get(HttpHeaderNames.HOST);
    if (host != null && regex != null) {
      String url;
      if (httpRequest.uri().indexOf("/") == 0) {
        if (httpRequest.uri().length() > 1) {
          url = host + httpRequest.uri();
        } else {
          url = host;
        }
      } else {
        url = httpRequest.uri();
      }
      return url.matches(regex);
    }
    return false;
  }

  /**
   * 检测头中的值是否为预期
   *
   * @param httpHeaders
   * @param name
   * @param regex
   * @return
   */
  public static boolean checkHeader(HttpHeaders httpHeaders, AsciiString name, String regex) {
    String s = httpHeaders.get(name);
    return s != null && s.matches(regex);
  }

  /**
   * 检测是否为请求网页资源
   */
  public static boolean isHtml(HttpRequest httpRequest, HttpResponse httpResponse) {
    String accept = httpRequest.headers().get(HttpHeaderNames.ACCEPT);
    String contentType = httpResponse.headers().get(HttpHeaderNames.CONTENT_TYPE);
    return httpResponse.status().code() == 200 && accept != null && accept
            .matches("^.*text/html.*$") && contentType != null && contentType
            .matches("^text/html.*$");
  }

}
