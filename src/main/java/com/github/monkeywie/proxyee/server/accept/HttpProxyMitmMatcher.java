package com.github.monkeywie.proxyee.server.accept;

import com.github.monkeywie.proxyee.util.ProtoUtil;

/**
 * @Author LiWei
 * @Description 用于匹配请求是否需要走中间人攻击
 * @Date 2023/06/07 11:11
 */
public interface HttpProxyMitmMatcher {
    /**
     * 客户端有新的连接建立时触发
     *
     * @param requestProto
     * @return 返回true表示走中间人攻击，返回false则直接转发
     */
    boolean doMatch(ProtoUtil.RequestProto requestProto);
}
