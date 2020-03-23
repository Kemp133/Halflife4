package com.halflife3.Networking.Packets;

import com.halflife3.Mechanics.Vector2;

import java.io.Serializable;
import java.net.InetAddress;

public class UniquePortPacket implements Serializable {

    private static final long serialVersionUID = 5L;
    private int port = 0;
    private InetAddress clientAddress;
    private Vector2 startPosition;

    public Vector2 getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(Vector2 startPosition) {
        this.startPosition = startPosition;
    }

    public InetAddress getClientAddress() {
        return clientAddress;
    }

    public void setClientAddress(InetAddress clientAddress) {
        this.clientAddress = clientAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
