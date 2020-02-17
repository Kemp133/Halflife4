package com.halflife3.Networking.Server;

import com.halflife3.Model.Vector2;
import com.halflife3.Networking.Packets.ConnectPacket;
import com.halflife3.Networking.Packets.DisconnectPacket;

import java.net.InetAddress;

public class EventListenerServer {

    public void received(Object packet, InetAddress sender) {

        if (packet instanceof ConnectPacket) {

            Server.addConnection(sender);

        } else if (packet instanceof DisconnectPacket) {

            Server.removeConnection(sender);

        } else if (packet instanceof Vector2) {

            Vector2 position = (Vector2) packet;
            ClientPositionHandlerServer.clientList.get(sender).setPosition(position);

        }

    }

}
