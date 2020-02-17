package com.halflife3.Networking.Client;

import com.halflife3.Model.Vector2;

public class ServerPositionHandlerClient {


    private static Vector2 server_position = new Vector2(300,300);
    private static Vector2 client_position = new Vector2(100,100);

    public static Vector2 getServer_position() {
        return server_position;
    }

    public static Vector2 getClient_position() {
        return client_position;
    }

    public static void setClient_position(Vector2 client_position) {
        ServerPositionHandlerClient.client_position = client_position;
    }

    public static void setServer_position(Vector2 server_position) {
        ServerPositionHandlerClient.server_position = server_position;
    }
}
