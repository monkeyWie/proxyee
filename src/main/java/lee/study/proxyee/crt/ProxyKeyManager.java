package lee.study.proxyee.crt;

import javax.net.ssl.X509KeyManager;
import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public final class ProxyKeyManager implements X509KeyManager {

    private String entryname;
    private X509Certificate caCert;
    private X509Certificate serverCert;
    private PrivateKey privatekey;

    public ProxyKeyManager(String entryname, X509Certificate caCert, X509Certificate serverCert, PrivateKey privatekey) {
        this.entryname = entryname;
        this.caCert = caCert;
        this.serverCert = serverCert;
        this.privatekey = privatekey;
    }

    public String[] getClientAliases(String string, Principal[] prncpls) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String chooseClientAlias(String[] strings, Principal[] prncpls, Socket socket) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String[] getServerAliases(String string, Principal[] prncpls) {
        return (new String[] {entryname});
    }

    public String chooseServerAlias(String string, Principal[] prncpls, Socket socket) {
        return entryname;
    }

    public X509Certificate[] getCertificateChain(String string) {
        X509Certificate x509certificates[] = new X509Certificate[2];
        x509certificates[0] = serverCert;
        x509certificates[1] = caCert;
        return x509certificates;
    }

    public PrivateKey getPrivateKey(String string) {
        return this.privatekey;
    }

    public static void main(String[] args) throws Exception {
       /* SSLContext sslcontext = SSLContext.getInstance("SSL");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate caCert = (X509Certificate)cf.generateCertificate(new FileInputStream("e:/ssl/ca.crt"));


        X509KeyManager[] x509km = new X509KeyManager[]{
                new ProxyKeyManager("test" , "20" , caCert)};
        sslcontext.init(x509km, null, null);

        SSLServerSocketFactory sslserversocketfactory = sslcontext.getServerSocketFactory();*/
    }
}
