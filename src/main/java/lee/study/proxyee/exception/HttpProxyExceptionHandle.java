package lee.study.proxyee.exception;

import io.netty.channel.Channel;

public class HttpProxyExceptionHandle{

  public void beforeCatch(Channel clientChannel, Throwable cause) {
    cause.printStackTrace();
  }

  public void afterCatch(Channel clientChannel, Channel proxyChannel, Throwable cause) {
    cause.printStackTrace();
  }
}
