package com.halflife3.Model;

import com.halflife3.Networking.Packets.PositionPacket;
import javafx.scene.transform.Affine;

public abstract class Controllable extends Sprite {
	protected String         ipOfClient;
	protected Affine         affine;
	protected short          rotation;
	protected Vector2        spawn_point;
	protected PositionPacket packetToSend;
	protected boolean        AI;
	protected boolean        moving = false;

	public Controllable(Vector2 position, Vector2 velocity) { super(position, velocity); }

	//region IP getter and setter
	public String getIpOfClient() {	return ipOfClient; }
	public void setIpOfClient(String ipOfClient) { this.ipOfClient = ipOfClient; }
	//endregion

	//region Rotation getter and setter
	public void setAffine(Affine affine) {
		this.affine = affine;
	}
	public Affine getAffine() {
		return affine;
	}
	public short getRotation() { return rotation; }
	public void setRotation(short rotation) { this.rotation = rotation;}
	//endregion

	//region SpawnPoint getter and setter
	public Vector2 getSpawn_point() {
		return spawn_point;
	}
	public void setSpawn_point(Vector2 spawn_point) {
		this.spawn_point = spawn_point;
	}
	//endregion

	//region AI getter and setter
	public boolean isAI() {
		return AI;
	}
	public void setAI(boolean AI) {
		this.AI = AI;
	}
	//endregion

	//region Get and Set Moving
	public boolean getMoving() { return moving; }

	public void setMoving(boolean moving) { this.moving = moving; }
	//endregion
}