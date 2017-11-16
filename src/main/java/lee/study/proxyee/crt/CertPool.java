package lee.study.proxyee.crt;

import lee.study.proxyee.server.HttpProxyServer;

import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

public class CertPool {

  private static Map<String, X509Certificate> certCache = new HashMap<>();

  public static X509Certificate getCert(String host) throws Exception {
    X509Certificate cert = null;
    if (host != null) {
      String key = host.trim().toLowerCase();
      if (certCache.containsKey(key)) {
        return certCache.get(key);
      } else {
        cert = CertUtil.genCert(HttpProxyServer.issuer, HttpProxyServer.serverPubKey,
            HttpProxyServer.caPriKey, key);
        certCache.put(key, cert);
      }
    }
    return cert;
  }
}
