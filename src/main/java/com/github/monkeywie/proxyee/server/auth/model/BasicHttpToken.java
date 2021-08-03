package com.github.monkeywie.proxyee.server.auth.model;

/**
 * @Author LiWei
 * @Description
 * @Date 2021/8/3 11:26
 */
public class BasicHttpToken implements HttpToken{
    private String usr;
    private String pwd;

    public BasicHttpToken() {
    }

    public BasicHttpToken(String usr, String pwd) {
        this.usr = usr;
        this.pwd = pwd;
    }

    public String getUsr() {
        return usr;
    }

    public void setUsr(String usr) {
        this.usr = usr;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    @Override
    public String toString() {
        return "HttpBasicToken{" +
                "usr='" + usr + '\'' +
                ", pwd='" + pwd + '\'' +
                '}';
    }
}
