package com.halflife3.Networking.Server;

import com.halflife3.Model.Vector2;

import java.net.InetAddress;
import java.util.HashMap;

public class ClientPositionHandlerServer {

    public static HashMap<InetAddress, ConnectedToServer> clientList = new HashMap<>();

    private static Vector2 server_position = new Vector2(300,300);

    public static Vector2 getServer_position() {
        return server_position;
    }

    public static void setServer_position(Vector2 server_position) {
        ClientPositionHandlerServer.server_position = server_position;
    }
}
