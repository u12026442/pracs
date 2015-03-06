package httpserver.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {

  public static void main(String[] args)
      throws IOException {
    @SuppressWarnings("resource")
    ServerSocket server = new ServerSocket(55555);
    System.out.println("Server Listening.");
    while (true) {
      Socket client = server.accept();
      new HttpClientHandler(client).start();
    }
  }

}
