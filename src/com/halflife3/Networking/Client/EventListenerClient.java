package com.halflife3.Networking.Client;

import com.halflife3.Networking.AddConnectionPacket;
import com.halflife3.Networking.RemoveConnectionPacket;

public class EventListenerClient {

    public void received(Object p) {
        if(p instanceof AddConnectionPacket) {
            AddConnectionPacket packet = (AddConnectionPacket)p;
            ConnectionHandlerClient.connections.put(packet.id,new ConnectionClient(packet.id));
            System.out.println(packet.id + " has connected");
        }else if(p instanceof RemoveConnectionPacket) {
            RemoveConnectionPacket packet = (RemoveConnectionPacket)p;
            System.out.println("Connection: " + packet.id + " has disconnected");
            ConnectionHandlerClient.connections.remove(packet.id);
        }
    }

}
