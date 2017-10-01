package io.unreach.israel.transport;

public class ServerTest {

    public static void main(String[] args) {
        Http2Server server = new Http2Server();
        server.initialize(8080);
    }

}
