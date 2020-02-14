package com.halflife3.Networking.Client;

public class MainClient {

    public static void main(String[] args) {

        Client client = new Client();
            client.joinGroup();
            client.getHostInfo();
            client.start();
    }

}
