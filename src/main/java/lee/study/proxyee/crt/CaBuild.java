package lee.study.proxyee.crt;

import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.X509Certificate;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

public class CaBuild {

    public static final long aDay = 1000*60*60*24;

    public static X509Certificate createAcIssuerCert(
            PublicKey pubKey,
            PrivateKey privKey)
            throws Exception
    {
        X509V3CertificateGenerator v3CertGen = new X509V3CertificateGenerator();
        //
        // signers name
        //
        String  issuer = "CN=My CA, OU=My CA, O=My, L=My, ST=AMy, C=CN";

        //
        // subjects name - the same as we are self signed.
        //
        String  subject = issuer;

        //
        // create the certificate - version 3
        //

        v3CertGen.setSerialNumber(BigInteger.valueOf(0x1234ABCDL));
        v3CertGen.setIssuerDN(new X509Principal(issuer));
        v3CertGen.setNotBefore(new Date(System.currentTimeMillis() - 30*aDay));
        v3CertGen.setNotAfter(new Date(System.currentTimeMillis() + 36500*aDay));
        v3CertGen.setSubjectDN(new X509Principal(subject));
        v3CertGen.setPublicKey(pubKey);
        v3CertGen.setSignatureAlgorithm("SHA1WithRSAEncryption");

        // Is a CA
        v3CertGen.addExtension(X509Extensions.BasicConstraints, true, new BasicConstraints(true));

        v3CertGen.addExtension(X509Extensions.SubjectKeyIdentifier, false, new SubjectKeyIdentifierStructure(pubKey));

        X509Certificate cert = v3CertGen.generateX509Certificate(privKey);

        cert.checkValidity(new Date());

        cert.verify(pubKey);

        return cert;
    }

    public static X509Certificate createClientCert(
            PublicKey       pubKey,
            PrivateKey      caPrivKey,
            PublicKey       caPubKey,
            String host)
            throws Exception
    {
        X509V3CertificateGenerator  v3CertGen = new X509V3CertificateGenerator();
        //
        // issuer
        //
        String  issuer = "C=CN, ST=GD, L=SZ, O=lee, OU=study, CN=testRoot";
        String  subuser = "C=CN, ST=GD, L=SZ, O=lee, OU=study, CN=baidu.com";

        //
        // subjects name table.
        //
        Hashtable attrs = new Hashtable();
        Vector order = new Vector();

        attrs.put(X509Principal.C, "CN");
        attrs.put(X509Principal.O, "My");
        attrs.put(X509Principal.OU, "My");
        attrs.put(X509Principal.CN, host);

        order.addElement(X509Principal.C);
        order.addElement(X509Principal.O);
        order.addElement(X509Principal.OU);
        order.addElement(X509Principal.CN);

        //
        // create the certificate - version 3
        //
        v3CertGen.reset();

        v3CertGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
        v3CertGen.setIssuerDN(new X509Principal(issuer));
        v3CertGen.setNotBefore(new Date(System.currentTimeMillis() - 10*aDay));
        v3CertGen.setNotAfter(new Date(System.currentTimeMillis() + 3650*aDay));
//        v3CertGen.setSubjectDN(new X509Principal(order, attrs));
        v3CertGen.setSubjectDN(new X509Principal(subuser));
        v3CertGen.setPublicKey(pubKey);
//        v3CertGen.setSignatureAlgorithm("SHA1WithRSAEncryption");
        v3CertGen.setSignatureAlgorithm("SHA256WithRSAEncryption");
        GeneralNames subjectAltName = new GeneralNames(new GeneralName(GeneralName.dNSName, "www.hao123.org"));
        v3CertGen.addExtension(X509Extensions.SubjectAlternativeName,true,subjectAltName);

        X509Certificate cert = v3CertGen.generateX509Certificate(caPrivKey);

        cert.checkValidity(new Date());

        cert.verify(caPubKey);

        return cert;
    }

    public static void main(String[] args) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        KeyPairGenerator caKeyPairGen = KeyPairGenerator.getInstance("RSA", "BC");
        caKeyPairGen.initialize(2048, new SecureRandom());
        KeyPair keypair = caKeyPairGen.genKeyPair();
        PublicKey serverPubKey = keypair.getPublic();

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        //openssl pkcs8 -topk8 -nocrypt -inform PEM -outform DER -in ca.key -out ca.pem
        EncodedKeySpec privateKeySpeckeySpec = new PKCS8EncodedKeySpec(Files.readAllBytes(Paths.get("e:/ssl/ca.pem")));
        PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpeckeySpec);

        //openssl rsa -in ca.pem -pubout -outform DER -out ca_pub.der
        EncodedKeySpec publickKeySpec = new X509EncodedKeySpec(Files.readAllBytes(Paths.get("e:/ssl/ca_pub.der")));
        PublicKey publicKey = keyFactory.generatePublic(publickKeySpec);

        X509Certificate certificate = createClientCert(serverPubKey,privateKey,publicKey,"aa");
        Files.write(Paths.get("e:/ssl/build.crt"),certificate.getEncoded());
    }
}
