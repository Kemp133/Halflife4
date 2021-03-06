package com.halflife3.GameObjects;

import com.halflife3.Networking.Packets.PositionPacket;
import javafx.scene.shape.*;

public abstract class Controllable extends Sprite {
	public    float          stunned    = 0;
	public    Circle         circle;
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
		spawnPosition       = new Vector2(position);
		deceleration        = new Vector2();
		packetToSend        = new PositionPacket();
		packetToSend.spawnX = position.getX();
		packetToSend.spawnY = position.getY();
		setSprite("Sprites/PlayerSkins/Cosmo_Hovering.png");
		circle = new Circle(position.getX() + getWidth() / 2 + 1, position.getY() + getHeight() / 2 + 1,
				Math.max(getWidth(), getHeight()) / 2 + 1);
	}

	@Override
	public Circle getBounds() {
		return circle;
	}

	//region IP getter and setter
	public String getIpOfClient() { return ipOfClient; }

	public void setIpOfClient(String ipOfClient) { this.ipOfClient = ipOfClient; }
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

	public void setDegrees(short degrees) { this.degrees = degrees;}

	public Vector2 getSpawnPosition() {
		return spawnPosition;
	}

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

	public void resetBasics() {
		stunned    = 0;
		bulletShot = false;
		isMoving   = false;
		holdsBall  = false;
		orgPos     = new Vector2(spawnPosition);
		position   = new Vector2(spawnPosition);
		circle.setCenterX(position.getX() + getWidth() / 2 + 1);
		circle.setCenterY(position.getY() + getHeight() / 2 + 1);
		resetVelocity();
	}

}
