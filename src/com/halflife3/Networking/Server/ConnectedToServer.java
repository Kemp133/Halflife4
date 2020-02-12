package com.halflife3.Networking.Server;

import com.halflife3.Model.Vector2;

import java.net.InetAddress;

public class ConnectedToServer {
    public int port;
    public InetAddress clientAddress;
    private Vector2 position;

    public ConnectedToServer(InetAddress address, int port) {
        this.port = port;
        this.clientAddress = address;
        position = new Vector2(0, 0); // startX and startY
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(Vector2 position) {
        this.position = position;
    }
}
