package lee.study.proxyee.util;

import java.io.IOException;
import java.io.InputStream;

public class LineBuffer {

  private int size;

  public LineBuffer(int size) {
    this.size = size;
  }

  public String readLine(InputStream input) throws IOException {
    int flag = 0;
    int index = 0;
    byte[] bts = new byte[this.size];
    int b;
    while (flag != 2 && (b = input.read()) != -1) {
      bts[index++] = (byte) b;
      if (b == '\r' && flag % 2 == 0) {
        flag++;
      } else if (b == '\n' && flag % 2 == 1) {
        flag++;
        if (flag == 2) {
          return new String(bts, 0, index - 2);
        }
      } else {
        flag = 0;
      }
      if (index == bts.length) {
        //满了扩容
        byte[] newBts = new byte[bts.length * 2];
        System.arraycopy(bts, 0, newBts, 0, bts.length);
        bts = null;
        bts = newBts;
      }
    }
    return null;
  }

  public static void main(String[] args) {
    System.out.println(2 % 2);
  }
}
