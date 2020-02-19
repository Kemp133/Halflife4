package com.halflife3.Networking.Client;

import com.halflife3.Networking.Packets.PositionListPacket;
import com.halflife3.Networking.Packets.UniquePortPacket;

public class EventListenerClient {

    public void received(Object packet) {

        if (packet instanceof UniquePortPacket) {

            UniquePortPacket portPacket = (UniquePortPacket) packet;

//            "If" for when multiple clients are already connected
            if (Client.clientAddress.getHostAddress()
                    .equals(portPacket.getClientAddress().getHostAddress())) {
                Client.setUniquePort(portPacket.getPort());
                Client.startingPosition = portPacket.getStartPosition();
            }

        } else if (packet instanceof PositionListPacket) {

            Client.listOfClients = (PositionListPacket) packet;

        }

    }

}
