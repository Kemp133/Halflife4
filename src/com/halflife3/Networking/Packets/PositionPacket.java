package com.halflife3.Networking.Packets;

import java.io.Serializable;

public class PositionPacket implements Serializable {

    private static final long serialVersionUID = 8L;

    public double posX, posY, velX, velY, spawnX, spawnY;
    public short degrees;
    public boolean bulletShot, holdsBall;
}
