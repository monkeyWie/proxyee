package com.github.monkeywie.proxyee.intercept.common;

import com.github.monkeywie.proxyee.crt.CertUtil;
import com.github.monkeywie.proxyee.intercept.HttpProxyIntercept;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptPipeline;
import com.github.monkeywie.proxyee.server.HttpProxyCACertFactory;
import com.github.monkeywie.proxyee.util.ProtoUtil.RequestProto;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.*;

import java.security.cert.X509Certificate;

/**
 * 处理证书下载页面 http://proxyServerIp:proxyServerPort
 */
public class CertDownIntercept extends HttpProxyIntercept {

    private boolean isDirect = false;

    private X509Certificate cert = null;

    /**
     * Using proxyee's own CA certificate to construct CertDownIntercept
     */
    public CertDownIntercept() {
    }

    /**
     * Using CA public key in {@linkplain HttpProxyCACertFactory CaCertFactory} to construct CertDownIntercept
     * <p> Visitors will receive a CA certificate from CaCertFactory instead of proxyee's built-in certificate.
     *
     * @param certFactory The same factory as {@link com.github.monkeywie.proxyee.server.HttpProxyServer}
     *                    ({@link com.github.monkeywie.proxyee.server.HttpProxyServer#caCertFactory(HttpProxyCACertFactory)})
     *                    is required, otherwise HTTP processing will fail
     * @throws Exception When factory throws an exception, it will be thrown directly.
     */
    public CertDownIntercept(HttpProxyCACertFactory certFactory) throws Exception {
        this.cert = certFactory.getCACert();
    }

    /**
     * Provide certificate structure CertDownIntercept directly.
     *
     * @param caCert The public key of CA certificate consistent with
     *               {@link com.github.monkeywie.proxyee.server.HttpProxyServer}.
     */
    public CertDownIntercept(X509Certificate caCert) {
        this.cert = caCert;
    }

    @Override
    public void beforeRequest(Channel clientChannel, HttpRequest httpRequest,
                              HttpProxyInterceptPipeline pipeline) throws Exception {
        RequestProto requestProto = pipeline.getRequestProto();
        if (!requestProto.getProxy()) {
            isDirect = true;
            if (httpRequest.uri().matches("^.*/ca.crt.*$")) {  //下载证书
                HttpResponse httpResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                        HttpResponseStatus.OK);

                byte[] bts = this.cert == null ? CertUtil
                        .loadCert(Thread.currentThread().getContextClassLoader().getResourceAsStream("ca.crt"))
                        .getEncoded() :
                        cert.getEncoded();

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
        if (!isDirect) {
            pipeline.beforeRequest(clientChannel, httpContent);
        }
    }
}
