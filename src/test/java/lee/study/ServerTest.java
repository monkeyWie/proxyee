package lee.study;

import java.io.IOException;

public class ServerTest {
    public static void main(String[] args) throws IOException {
        new NettyHttpProxyServer().start(9001);
        new NativeHttpProxyServer().start(9002);
    }
}
