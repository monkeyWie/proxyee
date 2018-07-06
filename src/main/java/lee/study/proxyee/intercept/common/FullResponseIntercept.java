package lee.study.proxyee.intercept.common;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import java.nio.charset.Charset;
import lee.study.proxyee.intercept.HttpProxyIntercept;
import lee.study.proxyee.intercept.HttpProxyInterceptInitializer;
import lee.study.proxyee.intercept.HttpProxyInterceptPipeline;
import lee.study.proxyee.server.HttpProxyServer;
import lee.study.proxyee.util.ByteUtil;

public abstract class FullResponseIntercept extends HttpProxyIntercept {

  //default max content length size is 8MB
  private static final int defaultMaxContentLength = 1024 * 1024 * 8;

  private int maxContentLength;

  public FullResponseIntercept() {
    this(defaultMaxContentLength);
  }

  public FullResponseIntercept(int maxContentLength) {
    this.maxContentLength = maxContentLength;
  }

  @Override
  public final void afterResponse(Channel clientChannel, Channel proxyChannel, HttpResponse httpResponse,
      HttpProxyInterceptPipeline pipeline) throws Exception {
    if (httpResponse instanceof FullHttpResponse) {
      handelResponse(pipeline.getHttpRequest(), (FullHttpResponse) httpResponse, pipeline);
    } else if (match(pipeline.getHttpRequest(), pipeline.getHttpResponse(), pipeline)) {
      pipeline.resetAfterHead();
      proxyChannel.pipeline().addAfter("httpCodec", "decompress", new HttpContentDecompressor());
      proxyChannel.pipeline().addAfter("decompress", "aggregator", new HttpObjectAggregator(maxContentLength));
      proxyChannel.pipeline().fireChannelRead(httpResponse);
      return;
    }
    pipeline.afterResponse(clientChannel, proxyChannel, httpResponse);
  }

  protected boolean isHtml(HttpRequest httpRequest, HttpResponse httpResponse) {
    String accept = httpRequest.headers().get(HttpHeaderNames.ACCEPT);
    String contentType = httpResponse.headers().get(HttpHeaderNames.CONTENT_TYPE);
    return httpResponse.status().code() == 200 && accept != null && accept
        .matches("^.*text/html.*$") && contentType != null && contentType
        .matches("^text/html.*$");
  }

  /**
   * 匹配到的响应会解码成FullResponse
   */
  public abstract boolean match(HttpRequest httpRequest, HttpResponse httpResponse, HttpProxyInterceptPipeline pipeline);

  /**
   * 拦截并处理响应
   */
  public abstract void handelResponse(HttpRequest httpRequest, FullHttpResponse httpResponse, HttpProxyInterceptPipeline pipeline);

  public static void main(String[] args) {
    new HttpProxyServer()
        .proxyInterceptInitializer(new HttpProxyInterceptInitializer() {
          @Override
          public void init(HttpProxyInterceptPipeline pipeline) {
            pipeline.addLast(new FullResponseIntercept() {

              @Override
              public boolean match(HttpRequest httpRequest, HttpResponse httpResponse, HttpProxyInterceptPipeline pipeline) {
                return true;
              }

              @Override
              public void handelResponse(HttpRequest httpRequest, FullHttpResponse httpResponse, HttpProxyInterceptPipeline pipeline) {
                //打印原始响应信息
                System.out.println(httpResponse.toString());
                System.out.println(httpResponse.content().toString(Charset.defaultCharset()));
                //修改响应头和响应体
                int index = ByteUtil.findText(httpResponse.content(), "<head>");
                ByteUtil.insertText(httpResponse.content(), index, "<script>alert(1)</script>");
                httpResponse.headers().set("handel", "edit head");
                if (httpResponse.headers().contains(HttpHeaderNames.CONTENT_LENGTH)) {
                  httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content().readableBytes());
                }
              }
            });
          }
        })
        .start(9999);
  }
}
