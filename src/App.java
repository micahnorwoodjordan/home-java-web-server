package src;

import java.io.IOException;

import src.server.Server;


public class App {
    public static void main(String[] args) throws IOException {
        Server myServer = new Server(8080);
        myServer.start();
    }
}