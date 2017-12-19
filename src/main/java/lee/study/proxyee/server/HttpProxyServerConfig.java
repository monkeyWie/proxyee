package lee.study.proxyee.server;

import io.netty.channel.EventLoopGroup;
import io.netty.handler.ssl.SslContext;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;

public class HttpProxyServerConfig {
  private SslContext clientSslCtx;
  private String issuer;
  private Date caNotBefore;
  private Date caNotAfter;
  private PrivateKey caPriKey;
  private PrivateKey serverPriKey;
  private PublicKey serverPubKey;
  private EventLoopGroup loopGroup;

  public SslContext getClientSslCtx() {
    return clientSslCtx;
  }

  public void setClientSslCtx(SslContext clientSslCtx) {
    this.clientSslCtx = clientSslCtx;
  }

  public String getIssuer() {
    return issuer;
  }

  public void setIssuer(String issuer) {
    this.issuer = issuer;
  }

  public Date getCaNotBefore() {
    return caNotBefore;
  }

  public void setCaNotBefore(Date caNotBefore) {
    this.caNotBefore = caNotBefore;
  }

  public Date getCaNotAfter() {
    return caNotAfter;
  }

  public void setCaNotAfter(Date caNotAfter) {
    this.caNotAfter = caNotAfter;
  }

  public PrivateKey getCaPriKey() {
    return caPriKey;
  }

  public void setCaPriKey(PrivateKey caPriKey) {
    this.caPriKey = caPriKey;
  }

  public PrivateKey getServerPriKey() {
    return serverPriKey;
  }

  public void setServerPriKey(PrivateKey serverPriKey) {
    this.serverPriKey = serverPriKey;
  }

  public PublicKey getServerPubKey() {
    return serverPubKey;
  }

  public void setServerPubKey(PublicKey serverPubKey) {
    this.serverPubKey = serverPubKey;
  }

  public EventLoopGroup getLoopGroup() {
    return loopGroup;
  }

  public void setLoopGroup(EventLoopGroup loopGroup) {
    this.loopGroup = loopGroup;
  }
}
