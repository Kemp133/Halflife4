package com.halflife3.Networking.Server;

import com.halflife3.Networking.Packets.ConnectPacket;

import java.net.InetAddress;

public class EventListenerServer {

    public void received(Object packet, InetAddress sender, int sendersPort) {

        if (packet instanceof ConnectPacket) {

            Server.addConnection(sender, sendersPort);

        }
        //TODO: "else if" for a Vector2 a.k.a Position packet event

    }

}
