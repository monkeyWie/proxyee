package lee.study.proxyee.proxy;

public class ProxyConfig {

  private ProxyType proxyType;
  private String host;
  private int port;
  private String user;
  private String pwd;

  public ProxyConfig(ProxyType proxyType, String host, int port) {
    this.proxyType = proxyType;
    this.host = host;
    this.port = port;
  }

  public ProxyConfig(ProxyType proxyType, String host, int port, String user, String pwd) {
    this.proxyType = proxyType;
    this.host = host;
    this.port = port;
    this.user = user;
    this.pwd = pwd;
  }

  public ProxyType getProxyType() {
    return proxyType;
  }

  public void setProxyType(ProxyType proxyType) {
    this.proxyType = proxyType;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getPwd() {
    return pwd;
  }

  public void setPwd(String pwd) {
    this.pwd = pwd;
  }
}
