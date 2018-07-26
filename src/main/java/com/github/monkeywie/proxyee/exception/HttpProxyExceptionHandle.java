package com.github.monkeywie.proxyee.exception;

import io.netty.channel.Channel;

public class HttpProxyExceptionHandle{

  public void beforeCatch(Channel clientChannel, Throwable cause) throws Exception {
    throw new Exception(cause);
  }

  public void afterCatch(Channel clientChannel, Channel proxyChannel, Throwable cause)
      throws Exception {
    throw new Exception(cause);
  }
}
