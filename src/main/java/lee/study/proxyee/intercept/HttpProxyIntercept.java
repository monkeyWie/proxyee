package lee.study.proxyee.intercept;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

public interface HttpProxyIntercept {
    boolean beforeRequest(Channel channel, HttpRequest httpRequest);

    boolean beforeRequest(Channel channel, HttpContent httpContent);

    boolean afterResponse(Channel channel, HttpResponse httpResponse);

    boolean afterResponse(Channel channel, HttpContent httpContent);
}
