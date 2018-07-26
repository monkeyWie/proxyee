package com.github.monkeywie.proxyee.intercept;

import com.github.monkeywie.proxyee.util.ProtoUtil.RequestProto;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class HttpProxyInterceptPipeline implements Iterable<HttpProxyIntercept> {

  private List<HttpProxyIntercept> intercepts;
  private HttpProxyIntercept defaultIntercept;

  private int posBeforeHead = 0;
  private int posBeforeContent = 0;
  private int posAfterHead = 0;
  private int posAfterContent = 0;

  private RequestProto requestProto;
  private HttpRequest httpRequest;
  private HttpResponse httpResponse;

  public HttpRequest getHttpRequest() {
    return httpRequest;
  }

  public HttpResponse getHttpResponse() {
    return httpResponse;
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
    this.httpRequest = httpRequest;
    if (this.posBeforeHead < intercepts.size()) {
      HttpProxyIntercept intercept = intercepts.get(this.posBeforeHead++);
      intercept.beforeRequest(clientChannel, this.httpRequest, this);
    }
    this.posBeforeHead = 0;
  }

  public void beforeRequest(Channel clientChannel, HttpContent httpContent) throws Exception {
    if (this.posBeforeContent < intercepts.size()) {
      HttpProxyIntercept intercept = intercepts.get(this.posBeforeContent++);
      intercept.beforeRequest(clientChannel, httpContent, this);
    }
    this.posBeforeContent = 0;
  }

  public void afterResponse(Channel clientChannel, Channel proxyChannel, HttpResponse httpResponse)
      throws Exception {
    this.httpResponse = httpResponse;
    if (this.posAfterHead < intercepts.size()) {
      HttpProxyIntercept intercept = intercepts.get(this.posAfterHead++);
      intercept.afterResponse(clientChannel, proxyChannel, this.httpResponse, this);
    }
    this.posAfterHead = 0;
  }

  public void afterResponse(Channel clientChannel, Channel proxyChannel, HttpContent httpContent)
      throws Exception {
    if (this.posAfterContent < intercepts.size()) {
      HttpProxyIntercept intercept = intercepts.get(this.posAfterContent++);
      intercept.afterResponse(clientChannel, proxyChannel, httpContent, this);
    }
    this.posAfterContent = 0;
  }

  public int posBeforeHead() {
    return this.posBeforeHead;
  }

  public int posBeforeContent() {
    return this.posBeforeContent;
  }

  public int posAfterHead() {
    return this.posAfterHead;
  }

  public int posAfterContent() {
    return this.posAfterContent;
  }

  public void posBeforeHead(int pos) {
    this.posBeforeHead = pos;
  }

  public void posBeforeContent(int pos) {
    this.posBeforeContent = pos;
  }

  public void posAfterHead(int pos) {
    this.posAfterHead = pos;
  }

  public void posAfterContent(int pos) {
    this.posAfterContent = pos;
  }

  public void resetBeforeHead() {
    posBeforeHead(0);
  }

  public void resetBeforeContent() {
    posBeforeContent(0);
  }

  public void resetAfterHead() {
    posAfterHead(0);
  }

  public void resetAfterContent() {
    posAfterContent(0);
  }

  @Override
  public Iterator<HttpProxyIntercept> iterator() {
    return intercepts.iterator();
  }
}
