package com.halflife3.GameObjects;

import com.halflife3.Mechanics.Vector2;
import com.halflife3.Networking.Packets.PositionPacket;

public abstract class Controllable extends Sprite {
	protected String         ipOfClient;
	protected short          degrees;
	protected Vector2        spawnPosition;
	protected Vector2        orgPos;
	protected PositionPacket packetToSend;
	protected boolean        isMoving   = false;
	protected boolean        holdsBall  = false;
	protected boolean        bulletShot = false;

	public Controllable(Vector2 position) {
		super(position, new Vector2());
		spawnPosition = new Vector2(position);
		deceleration  = new Vector2();
		setSprite("res/Sprites/PlayerSkins/Cosmo_Hovering.png");
	}

	//region IP getter and setter
	public String getIpOfClient() { return ipOfClient; }

	public void setIpOfClient(String ipOfClient) { this.ipOfClient = ipOfClient; }
	//endregion

	//region Rotation degrees getter and setter
	public void setDegrees(short degrees) { this.degrees = degrees;}

	public short getDegrees() { return degrees; }
	//endregion

	//region BulletShot getter and setter
	public boolean isBulletShot() {
		return bulletShot;
	}

	public void setBulletShot(boolean bulletShot) {
		this.bulletShot = bulletShot;
	}
	//endregion

	//region HoldingBall getter and setter
	public boolean isHoldingBall() {
		return holdsBall;
	}

	public void setHoldsBall(boolean holdsBall) {
		this.holdsBall = holdsBall;
	}
	//endregion

	public PositionPacket getPacketToSend() {
		packetToSend.posX       = getPosX();
		packetToSend.posY       = getPosY();
		packetToSend.velX       = getVelX();
		packetToSend.velY       = getVelY();
		packetToSend.degrees    = degrees;
		packetToSend.bulletShot = bulletShot;
		packetToSend.holdsBall  = holdsBall;
		return packetToSend;
	}
}
