package com.halflife3.Networking.Client;

import com.halflife3.Networking.AddConnectionPacket;

public class MainClient {

    public static void main(String[] args) {

        Client client = new Client("172.22.216.239",5000);
        client.connect();

        AddConnectionPacket packet = new AddConnectionPacket();
        client.sendObject(packet);

    }

}
