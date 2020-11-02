package com.github.monkeywie.proxyee.handler;

import com.github.monkeywie.proxyee.util.FlowCount;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import io.netty.handler.traffic.TrafficCounter;

import java.math.BigDecimal;
import java.net.InetSocketAddress;

/**
 * 流量统计和控制
 */
public class HttpProxyFlowHandle extends ChannelTrafficShapingHandler {


    String clientIP;
    Long  lastWrittenBytes;
    Long lastReadBytes;

    public HttpProxyFlowHandle(long writeLimit, long readLimit, long checkInterval, long maxTime) {
        super(writeLimit, readLimit, checkInterval, maxTime);
    }
    public HttpProxyFlowHandle(){
        super(FlowCount.writeLimit, FlowCount.readLimit, FlowCount.checkInterval, FlowCount.maxTime);
    }

    @Override
    protected void doAccounting(TrafficCounter counter) {
        lastWrittenBytes = counter.lastWrittenBytes();
        lastReadBytes = counter.lastReadBytes();
        BigDecimal b = new BigDecimal(lastWrittenBytes / 1024D / 1024D);
        Double d = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

        FlowCount flowCount = FlowCount.fm.get(clientIP);
        String name = FlowCount.uip.get(clientIP);
        if(flowCount == null){
            flowCount = new FlowCount();
            flowCount.setWriteFlow(d);
        }else{
            flowCount.setWriteFlow(flowCount.getWriteFlow()+d);
        }
        flowCount.setUserName(name);
        FlowCount.fm.put(clientIP, flowCount);

        super.doAccounting(counter);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
        this.clientIP = insocket.getAddress().getHostAddress();
        super.handlerAdded(ctx);
    }
}
