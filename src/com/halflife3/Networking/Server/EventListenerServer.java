package com.halflife3.Networking.Server;

import com.halflife3.Networking.Packets.ConnectPacket;
import com.halflife3.Networking.Packets.DisconnectPacket;
import com.halflife3.Networking.Packets.PositionPacket;

import java.net.InetAddress;

public class EventListenerServer {

    public void received(Object packet, InetAddress sender) {

        if (packet instanceof ConnectPacket) {

            Server.addConnection(sender);

        } else if (packet instanceof DisconnectPacket) {

            Server.removeConnection(sender);

        } else if (packet instanceof PositionPacket) {

            replaceEntry(sender.toString(), (PositionPacket) packet);

        }

    }

    public static synchronized void replaceEntry(String sender, PositionPacket pos) {
        ClientListServer.positionList.replace(sender, pos);
    }

}
