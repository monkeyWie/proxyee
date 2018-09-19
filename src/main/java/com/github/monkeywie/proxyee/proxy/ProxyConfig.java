package com.github.monkeywie.proxyee.proxy;

import java.io.Serializable;

public class ProxyConfig implements Serializable {

  private static final long serialVersionUID = 1531104384359036231L;

  private ProxyType proxyType;
  private String host;
  private int port;
  private String user;
  private String pwd;

  public ProxyConfig() {
  }

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

  @Override
  public String toString() {
    return "ProxyConfig{" +
        "proxyType=" + proxyType +
        ", host='" + host + '\'' +
        ", port=" + port +
        ", user='" + user + '\'' +
        ", pwd='" + pwd + '\'' +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ProxyConfig config = (ProxyConfig) o;

    if (port != config.port) {
      return false;
    }
    if (proxyType != config.proxyType) {
      return false;
    }
    if (host != null ? !host.equals(config.host) : config.host != null) {
      return false;
    }
    if (user != null ? !user.equals(config.user) : config.user != null) {
      return false;
    }
    return pwd != null ? pwd.equals(config.pwd) : config.pwd == null;
  }

  @Override
  public int hashCode() {
    int result = proxyType != null ? proxyType.hashCode() : 0;
    result = 31 * result + (host != null ? host.hashCode() : 0);
    result = 31 * result + port;
    result = 31 * result + (user != null ? user.hashCode() : 0);
    result = 31 * result + (pwd != null ? pwd.hashCode() : 0);
    return result;
  }
}
