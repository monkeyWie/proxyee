package lee.study;

import java.io.IOException;

public class ServerTest {
    public static void main(String[] args) throws IOException {
        new NettyHttpProxyServer().start(8999);
//        new NativeHttpProxyServer().start(8998);
    }
}
