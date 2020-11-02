package com.github.monkeywie.proxyee.intercept.common;

import com.github.monkeywie.proxyee.intercept.HttpProxyIntercept;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptPipeline;
import com.github.monkeywie.proxyee.util.FlowCount;
import com.github.monkeywie.proxyee.util.ProtoUtil;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.*;

import java.net.InetSocketAddress;
import java.util.Base64;

public class FullRequestAuthIntercept extends HttpProxyIntercept {

    @Override
    public void beforeRequest(Channel clientChannel, HttpRequest httpRequest, HttpProxyInterceptPipeline pipeline) throws Exception {
        ProtoUtil.RequestProto requestProto = ProtoUtil.getRequestProto(httpRequest);
        if (requestProto == null) { //bad request
            clientChannel.close();
            return;
        }

        HttpHeaders headers = httpRequest.headers();
        InetSocketAddress inetSocketAddress = (InetSocketAddress) clientChannel.localAddress();
        if(FlowCount.uip.get(inetSocketAddress.getAddress().getHostAddress()) == null && headers.get("Authorization") == null){
            System.out.println("=====================");
            HttpResponse httpResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK);
            httpResponse.setStatus(HttpResponseStatus.UNAUTHORIZED);
            httpResponse.headers().add("WWW-authenticate","Basic realm=\"\"");
            clientChannel.writeAndFlush(httpResponse);
            clientChannel.close();
        }else{
            if(headers.get("Authorization") != null){
                String authorization = headers.get("Authorization");
                System.out.println(authorization);
                byte[] decode = Base64.getDecoder().decode(headers.get("Authorization").split(" ")[1].getBytes());
                FlowCount.uip.put(inetSocketAddress.getAddress().getHostAddress(), new String(decode));
            }
            pipeline.beforeRequest(clientChannel, httpRequest);
        }

    }

    @Override
    public void beforeRequest(Channel clientChannel, HttpContent httpContent,
                              HttpProxyInterceptPipeline pipeline) throws Exception {
        clientChannel.pipeline().fireChannelActive();
        // pipeline.beforeRequest(clientChannel, httpContent);
    }
}
