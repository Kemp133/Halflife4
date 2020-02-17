package com.halflife3.Networking.Packets;

import java.io.Serializable;
import java.net.InetAddress;

public class UniquePortPacket  implements Serializable {

    private static final long serialVersionUID = 7L;
    private int port = 0;
    private InetAddress clientAddress;

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
