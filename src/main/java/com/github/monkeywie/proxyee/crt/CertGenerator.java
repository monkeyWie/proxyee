package com.github.monkeywie.proxyee.crt;

import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Date;

/**
 * 证书生成器接口.
 *
 * <p> 该接口用于在无法使用本库内置的 BC 加密套件时, 可自行实现具体加密细节以绕过 BC 加密套件.
 * <p> 实现后, 请注意对实现添加 {@link CertGeneratorInfo} 注解, 并按照 SPI 机制规范注册实现.
 *
 * @author LamGC
 */
public interface CertGenerator {

    /**
     * 生成服务端自签名证书.
     * @param issuer 元数据(X509 Names)
     * @param caPriKey 用于进行签名的 CA 私钥.
     * @param caNotBefore 证书生效时间, 在这个时间之前证书也是失效的.
     * @param caNotAfter 证书失效时间, 过了这个时间后证书即失效.
     * @param serverPubKey 服务端证书公钥.
     * @param hosts 证书所属域名.
     * @return 返回指定域名所属的服务端 X509 证书.
     */
    X509Certificate generateServerCert(String issuer, PrivateKey caPriKey, Date caNotBefore,
                                       Date caNotAfter, PublicKey serverPubKey,
                                       String... hosts) throws Exception;

    /**
     * 生成 CA 证书(自签名).
     * @param subject 元数据(X509 Names)
     * @param caNotBefore 证书生效时间, 在这个时间之前证书也是失效的.
     * @param caNotAfter 证书失效时间, 过了这个时间后证书即失效.
     * @param keyPair RSA 密钥对.
     * @return 返回自签名 CA 证书.
     */
    X509Certificate generateCaCert(String subject, Date caNotBefore, Date caNotAfter, KeyPair keyPair) throws Exception;

    /**
     * 生成新的密钥对.
     * @param algorithm 算法名
     * @param keySize 密钥长度.
     * @return 返回新的密钥对.
     * @throws NoSuchAlgorithmException 当指定算法不支持或不存在时抛出.
     * @throws InvalidParameterException 当密钥长度不支持时抛出.
     */
    KeyPair generateKeyPair(String algorithm, int keySize) throws NoSuchAlgorithmException, InvalidParameterException;

    /**
     * 构造一个生成器所属的 {@link KeyFactory}.
     * @return 返回 KeyFactory 对象.
     */
    KeyFactory generateKeyFactory();

}
