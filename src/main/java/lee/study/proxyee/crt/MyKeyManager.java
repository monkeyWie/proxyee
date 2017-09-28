package lee.study.proxyee.crt;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.X509KeyManager;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public final class MyKeyManager implements X509KeyManager {

    private String entryname;
    private String port;
    private X509Certificate caCert;
    private X509Certificate clientCert;
    private PrivateKey privatekey;

    MyKeyManager(String host, String port, X509Certificate caCert) {
        this.port = port;
        this.entryname = host;
        this.caCert = caCert;
        try {
            String certFileName = host + "_" + port + ".cert";
            FileInputStream caCertFis = new FileInputStream(certFileName);
            ObjectInputStream oos = new ObjectInputStream(caCertFis);
            privatekey = (PrivateKey) oos.readObject();
            clientCert = (X509Certificate) oos.readObject();
            oos.close();
            caCertFis.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public String[] getClientAliases(String string, Principal[] prncpls) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String chooseClientAlias(String[] strings, Principal[] prncpls, Socket socket) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String[] getServerAliases(String string, Principal[] prncpls) {
        return (new String[] {
                entryname
        });
    }

    public String chooseServerAlias(String string, Principal[] prncpls, Socket socket) {
        return entryname;
    }

    public X509Certificate[] getCertificateChain(String string) {
        X509Certificate x509certificates[] = new X509Certificate[2];

        x509certificates[0] = clientCert;
        x509certificates[1] = caCert;

        return x509certificates;
    }

    public PrivateKey getPrivateKey(String string) {
        return this.privatekey;
    }

    public static void main(String[] args) throws Exception {
        SSLContext sslcontext;
        sslcontext = SSLContext.getInstance("SSL");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate caCert = (X509Certificate)cf.generateCertificate(new FileInputStream("e:/ssl/ca.crt"));
        System.out.println(caCert.getPublicKey());
        X509KeyManager[] x509km = new X509KeyManager[]{
                new MyKeyManager("test" , "20" , caCert)};
        sslcontext.init(x509km, null, null);

        SSLServerSocketFactory sslserversocketfactory = sslcontext.getServerSocketFactory();
    }
}
