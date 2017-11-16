package lee.study.proxyee.server;

import lee.study.proxyee.util.LineBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

public class NativeHttpProxyServer {

  public void start(int port) throws IOException {
    ServerSocket serverSocket = new ServerSocket(port);
    for (; ; ) {
      new SocketHandle(serverSocket.accept()).start();
    }
  }

  static class SocketHandle extends Thread {

    private Socket socket;

    public SocketHandle(Socket socket) {
      this.socket = socket;
    }

    @Override
    public void run() {
      OutputStream clientOutput = null;
      InputStream clientInput = null;
      Socket proxySocket = null;
      InputStream proxyInput = null;
      OutputStream proxyOutput = null;
      CountDownLatch cdl = new CountDownLatch(1);
      try {
        clientInput = socket.getInputStream();
        clientOutput = socket.getOutputStream();
        String line;
        String host = "";
        LineBuffer lineBuffer = new LineBuffer(1024);
        StringBuilder headStr = new StringBuilder();
        while (null != (line = lineBuffer.readLine(clientInput))) {
          System.out.println(line);
          headStr.append(line + "\r\n");
          if (line.length() == 0) {
            break;
          } else {
            String[] temp = line.split(" ");
            if (temp[0].contains("Host")) {
              host = temp[1];
            }
          }
        }
        String type = headStr.substring(0, headStr.indexOf(" "));
        String[] hostTemp = host.split(":");
        host = hostTemp[0];
        int port = 80;
        if (hostTemp.length > 1) {
          port = Integer.valueOf(hostTemp[1]);
        }
        proxySocket = new Socket(host, port);
        proxyInput = proxySocket.getInputStream();
        proxyOutput = proxySocket.getOutputStream();
        if ("CONNECT".equalsIgnoreCase(type)) {//https
          clientOutput.write("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes());
          clientOutput.flush();
        } else {
          proxyOutput.write(headStr.toString().getBytes());
        }
        new ProxyHandleThread(clientInput, proxyOutput, cdl).start();
        while (true) {
          clientOutput.write(proxyInput.read());
        }
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        try {
          cdl.await();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        if (proxyInput != null) {
          try {
            proxyOutput.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
        if (proxyOutput != null) {
          try {
            proxyOutput.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
        if (proxySocket != null) {
          try {
            proxySocket.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
        if (clientInput != null) {
          try {
            clientInput.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
        if (clientOutput != null) {
          try {
            clientOutput.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
        if (socket != null) {
          try {
            socket.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }

    }
  }

  static class ProxyHandleThread extends Thread {

    private InputStream input;
    private OutputStream output;
    private CountDownLatch cdl;

    public ProxyHandleThread(InputStream input, OutputStream output, CountDownLatch cdl) {
      this.input = input;
      this.output = output;
      this.cdl = cdl;
    }

    @Override
    public void run() {
      try {
        while (true) {
          output.write(input.read());
        }
      } catch (IOException e) {
        this.cdl.countDown();
        e.printStackTrace();
      }
    }
  }
}
