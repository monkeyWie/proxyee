package com.github.monkeywie.proxyee.util;

import io.netty.buffer.ByteBuf;
import java.nio.charset.Charset;

public class ByteUtil {


  public static int findText(ByteBuf byteBuf, String str) {
    byte[] text = str.getBytes();
    int matchIndex = 0;
    for (int i = byteBuf.readerIndex(); i < byteBuf.readableBytes(); i++) {
      for (int j = matchIndex; j < text.length; j++) {
        if (byteBuf.getByte(i) == text[j]) {
          matchIndex = j + 1;
          if (matchIndex == text.length) {
            return i;
          }
        } else {
          matchIndex = 0;
        }
        break;
      }
    }
    return -1;
  }

  public static ByteBuf insertText(ByteBuf byteBuf, int index, String str) {
    return insertText(byteBuf, index, str, Charset.defaultCharset());
  }

  public static ByteBuf insertText(ByteBuf byteBuf, int index, String str, Charset charset) {
    byte[] begin = new byte[index + 1];
    byte[] end = new byte[byteBuf.readableBytes() - begin.length];
    byteBuf.readBytes(begin);
    byteBuf.readBytes(end);
    byteBuf.writeBytes(begin);
    byteBuf.writeBytes(str.getBytes(charset));
    byteBuf.writeBytes(end);
    return byteBuf;
  }
}
