package com.github.monkeywie.proxyee.util;

import io.netty.handler.codec.http.HttpRequest;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

public class ProtoUtil {

    /*
    代理服务器需要处理两种握手类型，一种是非CONNECT的http报文代理，另外一种是CONNECT的TCP报文原始转发
    示例：
        GET http://www.google.com/ HTTP/1.1
        CONNECT www.google.com:443 HTTP/1.1
        CONNECT echo.websocket.org:443 HTTP/1.1
        CONNECT echo.websocket.org:80 HTTP/1.1
    当客户端请求协议为TLS(https、wss)、WebSocket(ws)的时候，都会发起CONNECT请求进行原始转发，
    所以在握手的时候是无法区分客户端原始请求是否为TLS。
     */
    public static RequestProto getRequestProto(HttpRequest httpRequest) {
        RequestProto requestProto = new RequestProto();
        String uri = httpRequest.uri().toLowerCase();

        if (!uri.startsWith("http://")) {
            uri = "http://" + uri;
        }
        URL url;
        try {
            url = new URL(uri);
        } catch (MalformedURLException e) {
            return null;
        }

        requestProto.setHost(url.getHost());
        requestProto.setPort(url.getPort() != -1 ? url.getPort() : url.getDefaultPort());
        return requestProto;
    }

    public static class RequestProto implements Serializable {

        private static final long serialVersionUID = -6471051659605127698L;
        private String host;
        private int port;
        private boolean ssl;

        public RequestProto() {
        }

        public RequestProto(String host, int port, boolean ssl) {
            this.host = host;
            this.port = port;
            this.ssl = ssl;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public boolean getSsl() {
            return ssl;
        }

        public void setSsl(boolean ssl) {
            this.ssl = ssl;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RequestProto that = (RequestProto) o;
            return port == that.port &&
                    ssl == that.ssl &&
                    host.equals(that.host);
        }

        @Override
        public int hashCode() {
            return Objects.hash(host, port, ssl);
        }
    }
}
