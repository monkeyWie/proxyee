package com.github.monkeywie.proxyee.util;

import java.util.HashMap;
import java.util.Map;

public class FlowCount {

    String ip;
    String userName;
    Double writeFlow;
    Double readFlow;

    public static Long writeLimit = 1024 * 10L;
    public static Long readLimit = 1024 * 10L;
    public static Long checkInterval = 1000 * 1L;
    public static Long maxTime = 1000*10L;

    // 流量对应
    public static Map<String, FlowCount> fm = new HashMap<>();
    public static Map<String, String> uip = new HashMap<>();




    public String getIp() {
        return ip;
    }

    public FlowCount setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public Double getWriteFlow() {
        return writeFlow;
    }

    public FlowCount setWriteFlow(Double writeFlow) {
        this.writeFlow = writeFlow;
        return this;
    }

    public Double getReadFlow() {
        return readFlow;
    }

    public FlowCount setReadFlow(Double readFlow) {
        this.readFlow = readFlow;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public FlowCount setUserName(String userName) {
        this.userName = userName;
        return this;
    }
}
