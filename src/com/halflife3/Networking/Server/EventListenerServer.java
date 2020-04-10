package com.halflife3.Networking.Server;

import com.halflife3.Networking.Packets.ConnectPacket;
import com.halflife3.Networking.Packets.DisconnectPacket;
import com.halflife3.Networking.Packets.PositionPacket;

import java.net.InetAddress;

public class EventListenerServer {

    public void received(Object packet, InetAddress sender, Server server, ClientList cl) {

        if (packet instanceof ConnectPacket) {

            server.addConnection(sender);

        } else if (packet instanceof DisconnectPacket) {

            server.removeConnection(sender);

        } else if (packet instanceof PositionPacket) {

            replaceEntry(sender.toString(), (PositionPacket) packet, cl);

        }

    }

    public synchronized void replaceEntry(String sender, PositionPacket pos, ClientList cl) {
        cl.positionList.replace(sender, pos);
    }

}
