package lee.study.proxyee.crt;

import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.x509.X509V3CertificateGenerator;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class CertUtil {

    private static KeyFactory keyFactory = null;
    private static final long ONE_DAY = TimeUnit.DAYS.toMillis(1);

    private static KeyFactory getKeyFactory() throws NoSuchAlgorithmException {
        if (keyFactory == null) {
            keyFactory = KeyFactory.getInstance("RSA");
        }
        return keyFactory;
    }

    /**
     * 生成RSA公私密钥对,长度为2048
     *
     * @return
     * @throws Exception
     */
    public static KeyPair genKeyPair() throws Exception {
        KeyPairGenerator caKeyPairGen = KeyPairGenerator.getInstance("RSA", "BC");
        caKeyPairGen.initialize(2048, new SecureRandom());
        return caKeyPairGen.genKeyPair();
    }

    /**
     * 从文件加载RSA私钥
     * openssl pkcs8 -topk8 -nocrypt -inform PEM -outform DER -in ca.key -out ca_private.pem
     * @param bts
     * @return
     * @throws Exception
     */
    public static PrivateKey loadPriKey(byte[] bts) throws Exception {
        EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(bts);
        return getKeyFactory().generatePrivate(privateKeySpec);
    }

    /**
     * 从文件加载RSA私钥
     * openssl pkcs8 -topk8 -nocrypt -inform PEM -outform DER -in ca.key -out ca_private.pem
     * @param path
     * @return
     * @throws Exception
     */
    public static PrivateKey loadPriKey(String path) throws Exception {
        return loadPriKey(Files.readAllBytes(Paths.get(path)));
    }

    /**
     * 从文件加载RSA私钥
     * openssl pkcs8 -topk8 -nocrypt -inform PEM -outform DER -in ca.key -out ca_private.pem
     * @param uri
     * @return
     * @throws Exception
     */
    public static PrivateKey loadPriKey(URI uri) throws Exception {
        return loadPriKey(Paths.get(uri).toString());
    }

    /**
     * 从文件加载RSA私钥
     * openssl pkcs8 -topk8 -nocrypt -inform PEM -outform DER -in ca.key -out ca_private.pem
     * @param inputStream
     * @return
     * @throws Exception
     */
    public static PrivateKey loadPriKey(InputStream inputStream) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] bts = new byte[1024];
        int len;
        while((len=inputStream.read(bts))!=-1){
            outputStream.write(bts,0,len);
        }
        inputStream.close();
        outputStream.close();
        return loadPriKey(outputStream.toByteArray());
    }

    /**
     * 从文件加载RSA公钥
     * openssl rsa -in ca_private.pem -pubout -outform DER -out ca_pub.der
     *
     * @param bts
     * @return
     * @throws Exception
     */
    public static PublicKey loadPubKey(byte[] bts) throws Exception {
        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(bts);
        return getKeyFactory().generatePublic(publicKeySpec);
    }

    /**
     * 从文件加载RSA公钥
     * openssl rsa -in ca_private.pem -pubout -outform DER -out ca_pub.der
     *
     * @param path
     * @return
     * @throws Exception
     */
    public static PublicKey loadPubKey(String path) throws Exception {
        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Files.readAllBytes(Paths.get(path)));
        return getKeyFactory().generatePublic(publicKeySpec);
    }

    /**
     * 从文件加载RSA公钥
     * openssl rsa -in ca_private.pem -pubout -outform DER -out ca_pub.der
     * @param uri
     * @return
     * @throws Exception
     */
    public static PublicKey loadPubKey(URI uri) throws Exception {
        return loadPubKey(Paths.get(uri).toString());
    }

    /**
     * 从文件加载RSA公钥
     * openssl rsa -in ca_private.pem -pubout -outform DER -out ca_pub.der
     * @param inputStream
     * @return
     * @throws Exception
     */
    public static PublicKey loadPubKey(InputStream inputStream) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] bts = new byte[1024];
        int len;
        while((len=inputStream.read(bts))!=-1){
            outputStream.write(bts,0,len);
        }
        inputStream.close();
        outputStream.close();
        return loadPubKey(outputStream.toByteArray());
    }

    /**
     * 从文件加载证书
     * @param path
     * @return
     * @throws Exception
     */
    public static X509Certificate loadCert(InputStream inputStream) throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (X509Certificate)cf.generateCertificate(inputStream);
    }

    /**
     * 从文件加载证书
     * @param path
     * @return
     * @throws Exception
     */
    public static X509Certificate loadCert(String path) throws Exception {
        return loadCert(new FileInputStream(path));
    }

    /**
     * 从文件加载证书
     * @param uri
     * @return
     * @throws Exception
     */
    public static X509Certificate loadCert(URI uri) throws Exception {
        return loadCert(Paths.get(uri).toString());
    }

    /**
     * 动态生成服务器证书,并进行CA签授
     *
     * @param serverPubKey
     * @param caPriKey
     * @param caPubKey
     * @param host
     * @return
     * @throws Exception
     */
    public static X509Certificate genCert(PublicKey serverPubKey, PrivateKey caPriKey, PublicKey caPubKey, String host) throws Exception {
        X509V3CertificateGenerator v3CertGen = new X509V3CertificateGenerator();
        String issuer = "C=CN, ST=GD, L=SZ, O=lee, OU=study, CN=ProxyeeRoot";
        String subject = "C=CN, ST=GD, L=SZ, O=lee, OU=study, CN=" + host;
        v3CertGen.reset();
        v3CertGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
        v3CertGen.setIssuerDN(new X509Principal(issuer));
        v3CertGen.setNotBefore(new Date(System.currentTimeMillis() - 10 * ONE_DAY));
        v3CertGen.setNotAfter(new Date(System.currentTimeMillis() + 3650 * ONE_DAY));
        v3CertGen.setSubjectDN(new X509Principal(subject));
        v3CertGen.setPublicKey(serverPubKey);
        //SHA256 Chrome需要此哈希算法否则会出现不安全提示
        v3CertGen.setSignatureAlgorithm("SHA256WithRSAEncryption");
        //SAN扩展 Chrome需要此扩展否则会出现不安全提示
        GeneralNames subjectAltName = new GeneralNames(new GeneralName(GeneralName.dNSName, host));
        v3CertGen.addExtension(X509Extensions.SubjectAlternativeName, false, subjectAltName);
        X509Certificate cert = v3CertGen.generateX509Certificate(caPriKey);
        cert.checkValidity(new Date());
        cert.verify(caPubKey);
        return cert;
    }
}
