package com.github.monkeywie.proxyee.server.accept;

import com.github.monkeywie.proxyee.util.ProtoUtil;

import java.util.List;

/**
 * @Author LiWei
 * @Description 通过域名配置是否走中间人攻击
 * @Date 2023/06/07 11:11
 */
public class DomainHttpProxyMitmMatcher implements HttpProxyMitmMatcher{

    private List<String> domains;

    public DomainHttpProxyMitmMatcher(List<String> domains) {
        this.domains = domains;
    }

    @Override
    public boolean doMatch(ProtoUtil.RequestProto requestProto) {
        if(domains == null || domains.isEmpty()){
            return false;
        }
        return domains.stream().anyMatch(host -> requestProto.getHost().equals(host));
    }
}
