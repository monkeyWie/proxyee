package lee.study;

public class ServerTest {
    public static void main(String[] args) {
        new NettyHttpProxyServer().start(9888);
    }
}
