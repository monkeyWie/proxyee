package lee.study.proxyee.intercept;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import lee.study.proxyee.util.ProtoUtil.RequestProto;

public class HttpProxyInterceptPipeline implements Iterable<HttpProxyIntercept> {

  private List<HttpProxyIntercept> intercepts;
  private HttpProxyIntercept defaultIntercept;

  private int pos1 = 0;
  private int pos2 = 0;
  private int pos3 = 0;
  private int pos4 = 0;

  private RequestProto requestProto;
  private HttpRequest httpRequest;
  private HttpResponse httpResponse;

  public HttpRequest getHttpRequest() {
    return httpRequest;
  }

  public void setHttpRequest(HttpRequest httpRequest) {
    this.httpRequest = httpRequest;
  }

  public HttpResponse getHttpResponse() {
    return httpResponse;
  }

  public void setHttpResponse(HttpResponse httpResponse) {
    this.httpResponse = httpResponse;
  }

  public RequestProto getRequestProto() {
    return requestProto;
  }

  public void setRequestProto(RequestProto requestProto) {
    this.requestProto = requestProto;
  }

  public HttpProxyInterceptPipeline(HttpProxyIntercept defaultIntercept) {
    this.intercepts = new LinkedList<>();
    this.defaultIntercept = defaultIntercept;
    this.intercepts.add(defaultIntercept);
  }

  public void addLast(HttpProxyIntercept intercept) {
    this.intercepts.add(this.intercepts.size() - 1, intercept);
  }

  public void addFirst(HttpProxyIntercept intercept) {
    this.intercepts.add(0, intercept);
  }

  public HttpProxyIntercept get(int index) {
    return this.intercepts.get(index);
  }

  public HttpProxyIntercept getDefault() {
    return this.defaultIntercept;
  }

  public void beforeRequest(Channel clientChannel, HttpRequest httpRequest) throws Exception {
    if (this.pos1 == 0) {
      this.httpRequest = httpRequest;
    }
    if (this.pos1 < intercepts.size()) {
      HttpProxyIntercept intercept = intercepts.get(this.pos1++);
      intercept.beforeRequest(clientChannel, this.httpRequest, this);
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
    if (this.pos3 == 0) {
      this.httpResponse = httpResponse;
    }
    if (this.pos3 < intercepts.size()) {
      HttpProxyIntercept intercept = intercepts.get(this.pos3++);
      intercept.afterResponse(clientChannel, proxyChannel, this.httpResponse, this);
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

  public int pos1() {
    return this.pos1;
  }

  public int pos2() {
    return this.pos2;
  }

  public int pos3() {
    return this.pos3;
  }

  public int pos4() {
    return this.pos4;
  }

  public void pos1(int pos) {
    this.pos1 = pos;
  }

  public void pos2(int pos) {
    this.pos2 = pos;
  }

  public void pos3(int pos) {
    this.pos3 = pos;
  }

  public void pos4(int pos) {
    this.pos4 = pos;
  }

  public void reset1() {
    pos1(0);
  }

  public void reset2() {
    pos2(0);
  }

  public void reset3() {
    pos3(0);
  }

  public void reset4() {
    pos4(0);
  }

  @Override
  public Iterator<HttpProxyIntercept> iterator() {
    return intercepts.iterator();
  }
}
