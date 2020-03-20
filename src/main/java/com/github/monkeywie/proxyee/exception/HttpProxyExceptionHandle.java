package com.github.monkeywie.proxyee.exception;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class HttpProxyExceptionHandle {

    public void beforeCatch(Channel clientChannel, Throwable cause) throws Exception {
        throw new Exception(cause);
    }

    public void afterCatch(Channel clientChannel, Channel proxyChannel, Throwable cause)
            throws Exception {

        ByteArrayOutputStream baos = null;

        try {

            baos = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(baos);
            cause.printStackTrace(printStream);

            ByteBuf byteBuf = clientChannel.alloc().heapBuffer(baos.size());
            byteBuf.writeBytes(baos.toByteArray());

            HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.BAD_GATEWAY,byteBuf);
            clientChannel.writeAndFlush(response);
            if (clientChannel.pipeline().get("httpCodec") != null) {
                clientChannel.pipeline().remove("httpCodec");
            }

        } finally {

            if (baos != null) {
                baos.close();
            }

        }
    }
}
