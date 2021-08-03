package com.github.monkeywie.proxyee.server.context;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

/**
 * @Author LiWei
 * @Description
 * @Date 2021/8/3 11:33
 */
public class HttpContext {

    public static <T> T get(Channel channel, String key) {
        return channel.attr(AttributeKey.<T>valueOf(key)).get();
    }

    public static <T> void set(Channel channel, String key, T value) {
        channel.attr(AttributeKey.<T>valueOf(key)).set(value);
    }
}
