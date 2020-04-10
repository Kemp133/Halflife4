package com.halflife3.Networking.Client;

import com.halflife3.Networking.Packets.PositionListPacket;
import com.halflife3.Networking.Packets.UniquePortPacket;

public class EventListenerClient {

    public void received(Object packet, Client client) {

        if (packet instanceof UniquePortPacket) {

//            System.out.println("Received UniquePortPacket");
            UniquePortPacket portPacket = (UniquePortPacket) packet;

//            "If" for when multiple clients are already connected
            if (client.clientAddress.getHostAddress()
                    .equals(portPacket.getClientAddress().getHostAddress())) {
                client.setUniquePort(portPacket.getPort());
                client.startingPosition = portPacket.getStartPosition();
            }

        } else if (packet instanceof PositionListPacket) {

//            System.out.println("Received PositionListPacket");
            client.listOfClients = (PositionListPacket) packet;

        }

    }

}
