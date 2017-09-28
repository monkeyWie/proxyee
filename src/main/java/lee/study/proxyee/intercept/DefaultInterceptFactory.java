package lee.study.proxyee.intercept;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

public class DefaultInterceptFactory implements ProxyInterceptFactory {
    @Override
    public HttpProxyIntercept build() {
        return new HttpProxyIntercept() {

            @Override
            public boolean beforeRequest(Channel channel, HttpRequest httpRequest) {
                return false;
            }

            @Override
            public boolean beforeRequest(Channel channel, HttpContent httpContent) {
                return false;
            }

            @Override
            public boolean afterResponse(Channel channel, HttpResponse httpResponse) {
                return false;
            }

            @Override
            public boolean afterResponse(Channel channel, HttpContent httpContent) {
                return false;
            }
        };
    }
}
