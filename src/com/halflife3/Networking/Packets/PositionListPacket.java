package com.halflife3.Networking.Packets;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

public class PositionListPacket implements Serializable {

    private static final long serialVersionUID = 9L;
    public HashMap<String, PositionPacket> posList = new HashMap<>();
    public HashSet<String> connectedIPs = new HashSet<>();

    public HashSet<String> getConnectedIPs() {
        return connectedIPs;
    }

    public void setConnectedIPs(HashSet<String> connectedIPs) {
        this.connectedIPs = connectedIPs;
    }

    public HashMap<String, PositionPacket> getPosList() {
        return posList;
    }

    public void setPosList(HashMap<String, PositionPacket> posList) {
        this.posList = posList;
    }
}
