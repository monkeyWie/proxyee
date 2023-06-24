package com.github.monkeywie.proxyee.config;

/**
 * @Author ffei
 * @Date 2023/6/24 10:27
 */
public class IdleStateCheck {
    private long readerIdleTime;
    private long writerIdleTime;
    private long allIdleTime;

    public IdleStateCheck() {
        this.readerIdleTime = 10;
        this.writerIdleTime = 15;
        this.allIdleTime = 20;
    }

    public IdleStateCheck(long readerIdleTime, long writerIdleTime, long allIdleTime) {
        this.readerIdleTime = readerIdleTime;
        this.writerIdleTime = writerIdleTime;
        this.allIdleTime = allIdleTime;
    }

    public long getReaderIdleTime() {
        return readerIdleTime;
    }

    public long getWriterIdleTime() {
        return writerIdleTime;
    }

    public long getAllIdleTime() {
        return allIdleTime;
    }

    public void setReaderIdleTime(long readerIdleTime) {
        this.readerIdleTime = readerIdleTime;
    }

    public void setWriterIdleTime(long writerIdleTime) {
        this.writerIdleTime = writerIdleTime;
    }

    public void setAllIdleTime(long allIdleTime) {
        this.allIdleTime = allIdleTime;
    }
}

