package lee.study.proxyee.intercept;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class HttpProxyInterceptPipeline implements Iterable<HttpProxyIntercept> {

  private List<HttpProxyIntercept> intercepts;

  private int pos1 = 0;
  private int pos2 = 0;
  private int pos3 = 0;
  private int pos4 = 0;

  public HttpProxyInterceptPipeline(HttpProxyIntercept defaultIntercept) {
    this.intercepts = new LinkedList<>();
    this.intercepts.add(defaultIntercept);
  }

  public void addLast(HttpProxyIntercept intercept) {
    this.intercepts.add(this.intercepts.size() - 1, intercept);
  }

  public void addFirst(HttpProxyIntercept intercept) {
    this.intercepts.add(0, intercept);
  }

  public void beforeRequest(Channel clientChannel, HttpRequest httpRequest) throws Exception {
    if (this.pos1 < intercepts.size()) {
      HttpProxyIntercept intercept = intercepts.get(this.pos1++);
      intercept.beforeRequest(clientChannel, httpRequest, this);
    }
    this.pos1 = 0;
  }

  public void beforeRequest(Channel clientChannel, HttpContent httpContent) throws Exception {
    if (this.pos2 < intercepts.size()) {
      HttpProxyIntercept intercept = intercepts.get(this.pos2++);
      intercept.beforeRequest(clientChannel, httpContent, this);
    }
    this.pos2 = 0;
  }

  public void afterResponse(Channel clientChannel, Channel proxyChannel, HttpResponse httpResponse)
      throws Exception {
    if (this.pos3 < intercepts.size()) {
      HttpProxyIntercept intercept = intercepts.get(this.pos3++);
      intercept.afterResponse(clientChannel, proxyChannel, httpResponse, this);
    }
    this.pos3 = 0;
  }

  public void afterResponse(Channel clientChannel, Channel proxyChannel, HttpContent httpContent)
      throws Exception {
    if (this.pos4 < intercepts.size()) {
      HttpProxyIntercept intercept = intercepts.get(this.pos4++);
      intercept.afterResponse(clientChannel, proxyChannel, httpContent, this);
    }
    this.pos4 = 0;
  }

  @Override
  public Iterator<HttpProxyIntercept> iterator() {
    return intercepts.iterator();
  }
}
