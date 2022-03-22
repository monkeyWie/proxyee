package com.github.monkeywie.proxyee.crt.spi;

import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Date;

/**
 * <div class="zh">
 *   证书生成器接口.
 *   <p> 该接口用于在无法使用本库内置的 BC 加密套件时, 可自行实现具体加密细节以绕过 BC 加密套件.</p>
 *   <p> 实现后, 请注意对实现添加 {@link CertGeneratorInfo} 注解, 并按照 SPI 机制规范注册实现.</p>
 * </div>
 * <div class="en">
 *   Certificate generator interface.
 *   <p>This interface is used to implement specific encryption details to bypass the BC cipher suite when the built-in BC cipher suite of this library cannot be used.</p>
 *   <p>After implementation, please note to add {@link CertGeneratorInfo} annotation to the implementation, and register the implementation according to the SPI mechanism specification.</p>
 * </div>
 * @author LamGC
 */
public interface CertGenerator {

    /**
     * <div class="zh">生成服务端自签名证书.</div>
     * <div class="en">Generate a server-side self-signed certificate.</div>
     * @param issuer <div class="zh">元数据(X509 Names)</div><div class="en">Metadata (X509 Names)</div>
     * @param caPriKey <div class="zh">用于进行签名的 CA 私钥.</div><div class="en">CA private key used for signing.</div>
     * @param caNotBefore <div class="zh">证书生效时间, 在这个时间之前证书也是失效的.</div><div class="en">The validity time of the certificate, before this time the certificate is also invalid.</div>
     * @param caNotAfter <div class="zh">证书失效时间, 过了这个时间后证书即失效.</div><div class="en">The certificate expiration time, after which the certificate will become invalid.</div>
     * @param serverPubKey <div class="zh">服务端证书公钥.</div><div class="en">Server certificate public key.</div>
     * @param hosts <div class="zh">证书所属域名.</div><div class="en">The domain name to which the certificate belongs.</div>
     * @return <div class="zh">返回指定域名所属的服务端 X509 证书.</div><div class="en">Returns the server X509 certificate to which the specified domain name belongs.</div>
     * @throws Exception <div class="zh">当发生任意异常时, 异常将直接抛出至调用方.</div><div class="en">When any exception occurs, the exception will be thrown directly to the caller.</div>
     */
    X509Certificate generateServerCert(String issuer, PrivateKey caPriKey, Date caNotBefore,
                                       Date caNotAfter, PublicKey serverPubKey,
                                       String... hosts) throws Exception;

    /**
     * <div class="zh">生成 CA 证书(自签名).</div>
     * <div class="en">Generate a CA certificate (self-signed).</div>
     * @param subject <div class="zh">元数据(X509 Names)</div><div class="en">Metadata (X509 Names)</div>
     * @param caNotBefore <div class="zh">证书生效时间, 在这个时间之前证书也是失效的.</div><div class="en>The validity time of the certificate, before this time the certificate is also invalid.</div>
     * @param caNotAfter <div class="zh">证书失效时间, 过了这个时间后证书即失效.</div><div class="en">The certificate expiration time, after which the certificate will become invalid.</div>
     * @param keyPair <div class="zh">RSA 密钥对.</div><div class="en">RSA key pair.</div>
     * @return <div class="zh">返回自签名 CA 证书.</div><div class="en">Returns a self-signed CA certificate.</div>
     * @throws Exception <div class="zh">当发生任意异常时, 异常将直接抛出至调用方.</div><div class="en">When any exception occurs, the exception will be thrown directly to the caller.</div>
     */
    X509Certificate generateCaCert(String subject, Date caNotBefore, Date caNotAfter, KeyPair keyPair) throws Exception;

}
