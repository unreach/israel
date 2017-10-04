package io.unreach.israel.transport;

public class Http2ServerTest {

    public static void main(String[] args) {
        Http2Server server = new Http2Server();
        server.start(8080);
    }

}
