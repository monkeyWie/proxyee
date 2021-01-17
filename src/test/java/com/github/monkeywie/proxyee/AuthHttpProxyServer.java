package com.github.monkeywie.proxyee;

import com.github.monkeywie.proxyee.server.HttpProxyServer;
import com.github.monkeywie.proxyee.server.HttpProxyServerConfig;
import com.github.monkeywie.proxyee.server.auth.BasicHttpProxyAuthenticationProvider;

public class AuthHttpProxyServer {

    public static void main(String[] args) throws Exception {
        HttpProxyServerConfig config = new HttpProxyServerConfig();
        config.setAuthenticationProvider(new BasicHttpProxyAuthenticationProvider() {
            @Override
            protected boolean authenticate(String usr, String pwd) {
                return "admin".equals(usr) && "123456".equals(pwd);
            }
        });
        new HttpProxyServer()
                .serverConfig(config)
                .start(9999);
    }
}
