package com.halflife3.Networking.Server;

public class MainServer {

    public static void main(String[] args) {

        Server server = new Server(5000);
        server.start();

    }

}
