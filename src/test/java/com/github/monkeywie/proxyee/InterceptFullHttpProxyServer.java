package com.github.monkeywie.proxyee;

import com.github.monkeywie.proxyee.exception.HttpProxyExceptionHandle;
import com.github.monkeywie.proxyee.intercept.HttpProxyIntercept;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptInitializer;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptPipeline;
import com.github.monkeywie.proxyee.intercept.common.CertDownIntercept;
import com.github.monkeywie.proxyee.intercept.common.FullRequestIntercept;
import com.github.monkeywie.proxyee.intercept.common.FullResponseIntercept;
import com.github.monkeywie.proxyee.server.HttpProxyServer;
import com.github.monkeywie.proxyee.server.HttpProxyServerConfig;
import io.netty.buffer.ByteBuf;
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
                                return true;
                            }
                        });
                        pipeline.addLast(new FullResponseIntercept() {

                            @Override
                            public boolean match(HttpRequest httpRequest, HttpResponse httpResponse, HttpProxyInterceptPipeline pipeline) {
                                return true;
                            }

                        });
                        pipeline.addLast(new HttpProxyIntercept() {

                            private FullHttpRequest fullHttpRequest;

                            @Override
                            public void beforeRequest(Channel clientChannel, HttpRequest httpRequest, HttpProxyInterceptPipeline pipeline) throws Exception {
                                FullHttpRequest fullHttpRequest = (FullHttpRequest) httpRequest;
                                this.fullHttpRequest = new DefaultFullHttpRequest(fullHttpRequest.protocolVersion(),
                                        fullHttpRequest.method(),
                                        fullHttpRequest.uri(),
                                        fullHttpRequest.content().copy());
                                pipeline.beforeRequest(clientChannel, httpRequest);
                            }

                            @Override
                            public void afterResponse(Channel clientChannel, Channel proxyChannel, HttpResponse httpResponse, HttpProxyInterceptPipeline pipeline) throws Exception {
                                System.out.println(fullHttpRequest.toString());
                                System.out.println(this.fullHttpRequest.content().toString(Charset.defaultCharset()));
                                this.fullHttpRequest.release();

                                FullHttpResponse fullHttpResponse = (FullHttpResponse) httpResponse;
                                System.out.println(fullHttpResponse.toString());
                                System.out.println(fullHttpResponse.content().toString(Charset.defaultCharset()));
                                pipeline.afterResponse(clientChannel, proxyChannel, httpResponse);
                            }
                        });

                    }
                })
                .httpProxyExceptionHandle(new HttpProxyExceptionHandle() {
                                              @Override
                                              public void beforeCatch(Channel clientChannel, Throwable cause) throws Exception {
                                                  cause.printStackTrace();
                                              }

                                              @Override
                                              public void afterCatch(Channel clientChannel, Channel proxyChannel, Throwable cause) throws Exception {
                                                  cause.printStackTrace();
                                              }
                                          }
                )
                .start(9999);
    }
}
