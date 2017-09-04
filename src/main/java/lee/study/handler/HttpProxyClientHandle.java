package lee.study.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class HttpProxyClientHandle extends ChannelInboundHandlerAdapter {

    private Channel clientChannel;

    public HttpProxyClientHandle(Channel clientChannel) {
        this.clientChannel = clientChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        clientChannel.writeAndFlush(msg);
    }
}
