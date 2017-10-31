### java实现HTTP代理服务器
    支持HTTP、HTTPS、WebSocket,HTTPS采用动态签发证书,可以拦截http、https的报文并进行处理。
    例如：http(s)协议抓包,http(s)动态替换请求内容或响应内容等等。
### HTTPS支持
    需要导入项目中的CA证书(src/resources/ca.crt)至受信任的根证书颁发机构.
#### 原生java实现(仅做为demo，不支持拦截)
```
    new NativeHttpProxyServer().start(9002);
```
#### 基于netty实现
```
    new NettyHttpProxyServer().start(9001);
    //拦截处理
    new NettyHttpProxyServer().initProxyInterceptFactory(() -> new HttpProxyIntercept() {
    
        @Override
        public boolean afterResponse(Channel clientChannel, Channel proxyChannel, HttpResponse httpResponse) {
            //拦截响应，添加一个响应头
            httpResponse.headers().add("intercept","test");
            return true;
        }

    }).start(9999);
```

#### 流程
SSL握手

![SSL握手](https://sfault-image.b0.upaiyun.com/751/727/751727588-59ccbe3293bef_articlex)

HTTP通讯

![HTTP通讯](https://sfault-image.b0.upaiyun.com/114/487/1144878844-59ccbe42037b6_articlex)