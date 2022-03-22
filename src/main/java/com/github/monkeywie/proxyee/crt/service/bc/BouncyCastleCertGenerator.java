package com.github.monkeywie.proxyee.crt.service.bc;

import com.github.monkeywie.proxyee.crt.spi.CertGenerator;
import com.github.monkeywie.proxyee.crt.spi.CertGeneratorInfo;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <div class="zh">使用了 BC 套件的证书生成器.</div>
 * <div class="en">Use the BouncyCastle suite's certificate generator.</div>
 *
 * @author LamGC
 */
@CertGeneratorInfo(name = "BouncyCastle")
public class BouncyCastleCertGenerator implements CertGenerator {

    static {
        // 注册BouncyCastleProvider加密库
        // Register the BouncyCastleProvider cryptographic library
        Security.addProvider(new BouncyCastleProvider());
    }

    private static KeyFactory keyFactory = null;

    @Override
    public X509Certificate generateServerCert(String issuer, PrivateKey caPriKey, Date caNotBefore,
                                          Date caNotAfter, PublicKey serverPubKey,
                                          String... hosts) throws Exception {
        /* String issuer = "C=CN, ST=GD, L=SZ, O=lee, OU=study, CN=ProxyeeRoot";
        String subject = "C=CN, ST=GD, L=SZ, O=lee, OU=study, CN=" + host;*/
        // 根据CA证书subject来动态生成目标服务器证书的issuer和subject
        // Dynamically generate the issuer and subject of the target server certificate based on the CA certificate subject
        String subject = Stream.of(issuer.split(", ")).map(item -> {
            String[] arr = item.split("=");
            if ("CN".equals(arr[0])) {
                return "CN=" + hosts[0];
            } else {
                return item;
            }
        }).collect(Collectors.joining(", "));

        // doc from https://www.cryptoworkshop.com/guide/
        JcaX509v3CertificateBuilder jv3Builder = new JcaX509v3CertificateBuilder(new X500Name(issuer),
                //issue#3 修复ElementaryOS上证书不安全问题(serialNumber为1时证书会提示不安全)，避免serialNumber冲突，采用时间戳+4位随机数生成
                // Fix the insecure certificate issue on ElementaryOS (when serialNumber is 1, the certificate will be
                // prompted to be insecure), avoid serialNumber conflict, and use timestamp + 4-digit random number to
                // generate
                BigInteger.valueOf(System.currentTimeMillis() + (long) (Math.random() * 10000) + 1000),
                caNotBefore,
                caNotAfter,
                new X500Name(subject),
                serverPubKey);
        // SAN扩展证书支持的域名，否则浏览器提示证书不安全
        // The domain name supported by the SAN extended certificate, otherwise the browser prompts that the certificate is not secure
        GeneralName[] generalNames = new GeneralName[hosts.length];
        for (int i = 0; i < hosts.length; i++) {
            generalNames[i] = new GeneralName(GeneralName.dNSName, hosts[i]);
        }
        GeneralNames subjectAltName = new GeneralNames(generalNames);
        jv3Builder.addExtension(Extension.subjectAlternativeName, false, subjectAltName);
        // SHA256 用SHA1浏览器可能会提示证书不安全
        // SHA256 and SHA1 browsers may prompt that the certificate is not secure
        ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSAEncryption").build(caPriKey);
        return new JcaX509CertificateConverter().getCertificate(jv3Builder.build(signer));
    }

    @Override
    public X509Certificate generateCaCert(String subject, Date caNotBefore, Date caNotAfter,
                                            KeyPair keyPair) throws CertIOException, OperatorCreationException, CertificateException {
        JcaX509v3CertificateBuilder jv3Builder = new JcaX509v3CertificateBuilder(new X500Name(subject),
                BigInteger.valueOf(System.currentTimeMillis() + (long) (Math.random() * 10000) + 1000),
                caNotBefore,
                caNotAfter,
                new X500Name(subject),
                keyPair.getPublic());
        jv3Builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(0));
        ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSAEncryption")
                .build(keyPair.getPrivate());
        return new JcaX509CertificateConverter().getCertificate(jv3Builder.build(signer));
    }

}
