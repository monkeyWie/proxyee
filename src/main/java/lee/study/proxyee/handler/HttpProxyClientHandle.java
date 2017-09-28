package lee.study.proxyee.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import lee.study.proxyee.intercept.HttpProxyIntercept;

public class HttpProxyClientHandle extends ChannelInboundHandlerAdapter {

    private Channel clientChannel;
    private HttpProxyIntercept httpProxyHook;

    public HttpProxyClientHandle(Channel clientChannel) {
        this.clientChannel = clientChannel;
        this.httpProxyHook = ((HttpProxyServerHandle) clientChannel.pipeline().get("serverHandle")).getHttpProxyHook();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        HttpResponse response = null;
        if(msg instanceof HttpResponse){
            response = (HttpResponse) msg;
            if(httpProxyHook.afterResponse(clientChannel,response)){
                return;
            }
//            response.headers().set(HttpHeaderNames.CONTENT_TYPE,"application/x-jpg");
            //response.headers().set("Content-Type","application/x-jpg");
            /*downFlag = DownUtil.download(httpProxyServerHandle.getHttpRequest(),response);
            if(downFlag){
                clientChannel.writeAndFlush(response);
                clientChannel.writeAndFlush(new DefaultLastHttpContent());
            }*/
        }else if(msg instanceof HttpContent){
            if(httpProxyHook.afterResponse(clientChannel,(HttpContent) msg)){
                return;
            }
        }
        clientChannel.writeAndFlush(msg);
        if(response!=null){
            if(HttpHeaderValues.WEBSOCKET.toString().equals(response.headers().get(HttpHeaderNames.UPGRADE))){
                //websocket转发原始报文
                ctx.pipeline().remove("httpCodec");
                clientChannel.pipeline().remove("httpCodec");
            }

        }
    }
}
