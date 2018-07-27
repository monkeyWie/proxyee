### HTTP代理服务器
    支持HTTP、HTTPS、WebSocket,HTTPS采用动态签发SSL证书,可以拦截http、https的报文并进行处理。
    例如：http(s)协议抓包,http(s)动态替换请求内容或响应内容等等。
#### HTTPS支持
    需要导入项目中的CA证书(src/resources/ca.crt)至受信任的根证书颁发机构。
    可以使用CertDownIntercept拦截器，开启网页下载证书功能，访问http://serverIP:serverPort即可进入。
    注：安卓手机上安装证书若弹出键入凭据存储的密码，输入锁屏密码即可。
#### 二级代理
    可设置二级代理服务器,支持http,socks4,socks5。
#### 启动
```
//启动一个普通的http代理服务器，不解密https
new HttpProxyServer().start(9999);
```
```
//启动一个解密https的代理服务器，并且拦截百度首页，注入js和修改响应头
//当开启了https解密时，需要安装CA证书(`src/resources/ca.crt`)至受信任的根证书颁发机构。
HttpProxyServerConfig config =  new HttpProxyServerConfig();
config.setHandleSsl(true);
new HttpProxyServer()
    .serverConfig(config)
    .proxyInterceptInitializer(new HttpProxyInterceptInitializer() {
      @Override
      public void init(HttpProxyInterceptPipeline pipeline) {
        pipeline.addLast(new FullResponseIntercept() {

          @Override
          public boolean match(HttpRequest httpRequest, HttpResponse httpResponse, HttpProxyInterceptPipeline pipeline) {
            //在匹配到百度首页时插入js
            return HttpUtil.checkUrl(pipeline.getHttpRequest(), "^www.baidu.com$")
                && isHtml(httpRequest, httpResponse);
          }

          @Override
          public void handelResponse(HttpRequest httpRequest, FullHttpResponse httpResponse, HttpProxyInterceptPipeline pipeline) {
            //打印原始响应信息
            System.out.println(httpResponse.toString());
            System.out.println(httpResponse.content().toString(Charset.defaultCharset()));
            //修改响应头和响应体
            httpResponse.headers().set("handel", "edit head");
            /*int index = ByteUtil.findText(httpResponse.content(), "<head>");
            ByteUtil.insertText(httpResponse.content(), index, "<script>alert(1)</script>");*/
            httpResponse.content().writeBytes("<script>alert('hello proxyee')</script>".getBytes());
          }
        });
      }
    })
    .start(9999);
```
更多demo代码在test包内可以找到，这里就不一一展示了

#### 流程
SSL握手

![SSL握手](https://sfault-image.b0.upaiyun.com/751/727/751727588-59ccbe3293bef_articlex)

HTTP通讯

![HTTP通讯](https://sfault-image.b0.upaiyun.com/114/487/1144878844-59ccbe42037b6_articlex)