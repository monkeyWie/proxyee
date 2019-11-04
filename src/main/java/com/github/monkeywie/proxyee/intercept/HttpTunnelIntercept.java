package com.github.monkeywie.proxyee.intercept;

import com.github.monkeywie.proxyee.util.ProtoUtil;

/**
 * @Author LiWei
 * @Description 用于拦截隧道请求，在代理服务器与目标服务器连接前
 * @Date 2019/11/4 9:57
 */
public interface HttpTunnelIntercept {
    void handle(ProtoUtil.RequestProto requestProto);
}
