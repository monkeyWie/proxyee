package lee.study.proxyee.util;

import io.netty.handler.codec.http.HttpRequest;

public class HttpUtil {

    public static RequestProto getRequestProto(HttpRequest httpRequest){
        RequestProto requestProto = new RequestProto();
        String hostStr = httpRequest.headers().get("host");
        String[] temp = hostStr.split(":");
        int port = 80;
        if (temp.length > 1) {
            port = Integer.parseInt(temp[1]);
        } else {
            if (httpRequest.uri().indexOf("https") == 0) {
                port = 443;
            }
        }
        requestProto.setHost(temp[0]);
        requestProto.setPort(port);
        return requestProto;
    }

    public static class RequestProto{
        private String host;
        private int port;

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
    }
}
