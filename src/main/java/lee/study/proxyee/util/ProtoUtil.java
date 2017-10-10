package lee.study.proxyee.util;

import io.netty.handler.codec.http.HttpRequest;

public class ProtoUtil {

    public static RequestProto getRequestProto(HttpRequest httpRequest){
        RequestProto requestProto = new RequestProto();
        String hostStr = httpRequest.headers().get("host");
        String[] temp = hostStr.split(":");
        boolean isSsl = httpRequest.uri().indexOf("https") == 0;
        int port = 80;
        if (temp.length > 1) {
            port = Integer.parseInt(temp[1]);
        } else {
            if (isSsl) {
                port = 443;
            }
        }
        requestProto.setHost(temp[0]);
        requestProto.setPort(port);
        requestProto.setSsl(isSsl);
        return requestProto;
    }

    public static class RequestProto{
        private String host;
        private int port;
        private boolean ssl;

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
    }
}
