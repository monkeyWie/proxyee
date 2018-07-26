package com.github.monkeywie.proxyee.util;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;

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

}
