<div align="center">
  <h1>Proxyee</h1>
  <p>
  
[![maven](https://img.shields.io/maven-central/v/com.github.monkeywie/proxyee.svg)](https://search.maven.org/search?q=com.github.monkeywie)
[![license](https://img.shields.io/github/license/monkeywie/proxyee.svg)](https://opensource.org/licenses/MIT)

  </p>
  <p>

[English](/README.md) | [中文](/README_zh-CN.md)

  </p>
</div>

---

## Introduction

Proxyee is a JAVA written HTTP proxy server library that supports HTTP, HTTPS, Websocket protocols, and supports MITM (Man-in-the-middle), which can capture and tamper with HTTP, HTTPS packet.

## Usage

```xml
<dependency>
    <groupId>com.github.monkeywie</groupId>
    <artifactId>proxyee</artifactId>
    <version>1.6.9</version>
</dependency>
```

## IP pools

[https://www.ipidea.net](http://www.ipidea.net/?utm-source=monkeyWie&utm-keyword=?monkeyWie)

> IPIDEA 致力于为全球用户提供优质的大数据代理服务，促进信息创造价值。
> 
> 优势：
> 1. 覆盖 220+的国家和地区，9000 万真实住宅 IP 资源，汇聚成大规模代理服务池。
> 2. 提供动态住宅代理、静态住宅代理、数据中心、移动代理等多种解决方案，满足电子商务、市场调查、抓取索引、网站测试、广告验证、seo 监控优化等多个业务场景。
> 3. 支持 HTTP/HTTPS/Socks5 协议
> 4. 真实住宅 IP，支持从制定国家城市访问目标网站，隐藏真实网络环境，保护隐私，24 小时持续过滤并更新，IP 纯净度高，快速响应，无限并发，99.9%的成功率，确保高效稳定连接
> 5. 支持海量 IP 免费试用

## Demo

- Normal HTTP proxy

```java
new HttpProxyServer().start(9999);
```

- MITM HTTP proxy

The following is a demonstration of a MITM attack that modifies the response header and response body when visiting the Baidu homepage, as shown in the figure below：

![20200724152245](https://raw.githubusercontent.com/monkeyWie/pic-bed/master/proxyee/20200724152245.png)

Code：

```java
HttpProxyServerConfig config =  new HttpProxyServerConfig();
//enable HTTPS support
//If not enabled, HTTPS will not be intercepted, but forwarded directly to the raw packet.
config.setHandleSsl(true);
new HttpProxyServer()
    .serverConfig(config)
    .proxyInterceptInitializer(new HttpProxyInterceptInitializer() {
      @Override
      public void init(HttpProxyInterceptPipeline pipeline) {
        pipeline.addLast(new FullResponseIntercept() {

          @Override
          public boolean match(HttpRequest httpRequest, HttpResponse httpResponse, HttpProxyInterceptPipeline pipeline) {
            //Insert js when matching to Baidu homepage
            return HttpUtil.checkUrl(pipeline.getHttpRequest(), "^www.baidu.com$")
                && isHtml(httpRequest, httpResponse);
          }

          @Override
          public void handleResponse(HttpRequest httpRequest, FullHttpResponse httpResponse, HttpProxyInterceptPipeline pipeline) {
            //Print raw packet
            System.out.println(httpResponse.toString());
            System.out.println(httpResponse.content().toString(Charset.defaultCharset()));
            //Edit response header and response body
            httpResponse.headers().set("handel", "edit head");
            httpResponse.content().writeBytes("<script>alert('hello proxyee')</script>".getBytes());
          }
        });
      }
    })
    .start(9999);
```

> Note: When https support is enabled, you need to install the CA certificate (`src/resources/ca.crt`) to a trusted root certificate authority.

More demo code can be found in the test package.

## HTTPS support

The CA certificate (src/resources/ca.crt) from the project needs to be imported to a trusted root certificate authority.
You can use the CertDownIntercept interceptor to enable the web certificate download feature, visit http://serverIP:serverPort to access.

> Note 1: If the certificate installation on Android phones pops up the password stored in your credentials, just enter the lock screen password.
>
> Note 2: Android 7 and above, the system no longer trusts user-installed certificates, you need to root and use the
> cat ca.crt > $(openssl x509 -inform PEM -subject_hash_old -in ca.crt | head -1).0
> command generates the d1488b25.0 file, and then moves the file to the
> /system/etc/security/cacerts/
> And give 644 access.
>
> Note 3: In Android 7 and above, even if you add the certificate to the system certificate, this certificate does not work in chrome. The reason is that chrome will only trust certificates with validity less than 27 months from 2018 (https://www.entrustdatacard.com/blog/2018/february/chrome-requires-ct-after-april-2018). So you need to generate the certificate file yourself.

### Custom CA

Since the root certificate and private key attached to the project are public, they are only suitable for local development and debugging, please generate your own root certificate and private key when using in the official environment, otherwise there will be risks.

- running the main method of the`com.github.monkeywie.proxyee.crt.CertUtil` class

- use openssl

```sh
openssl genrsa -out ca.key 2048
openssl pkcs8 -topk8 -nocrypt -inform PEM -outform DER -in ca.key -out ca_private.der
openssl req -sha256 -new -x509 -days 365 -key ca.key -out ca.crt \
    -subj "/C=CN/ST=GD/L=SZ/O=lee/OU=study/CN=testRoot"
```

Copy `ca.crt` and `ca_private.der` to the project src/resources/ after generation, or implement the HttpProxyCACertFactory interface to custom load the root certificate and private key.

## Authentication

Currently only basic authentication are supported.

- Basic

```java
// curl -i -x 127.0.0.1:9999 -U admin:123456 http://www.baidu.com
HttpProxyServerConfig config = new HttpProxyServerConfig();
        config.setAuthenticationProvider(new BasicHttpProxyAuthenticationProvider() {
            @Override
            protected BasicHttpToken authenticate(String usr, String pwd) {
                if ("admin".equals(usr) && "123456".equals(pwd)) {
                    return new BasicHttpToken(usr, pwd);
                }
                return null;
            }
        });
new HttpProxyServer()
        .serverConfig(config)
        .start(9999);
```

- Custom

Customize authentication by implementing the `HttpProxyAuthenticationProvider` interface.

### Authentication context

After the authorization, the token returned from the verification pass can be obtained in the pipeline.

```java
HttpToken token = HttpAuthContext.getToken(clientChannel);
```

## Pre-proxy support

Pre-proxy can be set,support http,socks4,socks5 protocol.

```java
new HttpProxyServer()
    .proxyConfig(new ProxyConfig(ProxyType.SOCKS5, "127.0.0.1", 1085))
    .start(9999);
```

## Flow

SSL handshake
![SSL握手](https://raw.githubusercontent.com/monkeyWie/pic-bed/master/proxyee/20190918134332.png)

HTTP communication

![HTTP通讯](https://raw.githubusercontent.com/monkeyWie/pic-bed/master/proxyee/20190918134232.png)

## How it works

- [JAVA 写 HTTP 代理服务器(一)-socket 实现](https://segmentfault.com/a/1190000011810997)
- [JAVA 写 HTTP 代理服务器(二)-netty 实现](https://segmentfault.com/a/1190000011811082)
- [JAVA 写 HTTP 代理服务器(三)-https 明文捕获](https://segmentfault.com/a/1190000011811150)

## Thanks

[![intellij-idea](idea.svg)](https://www.jetbrains.com/?from=proxyee)
