package com.halflife3.Networking.Packets;

import java.io.Serializable;

public class PositionPacket implements Serializable {

    private static final long serialVersionUID = 8L;

    public double orgPosX, orgPosY, velX, velY, spawnX, spawnY, rotation;

}
