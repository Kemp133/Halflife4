package com.halflife3.Mechanics.GameObjects;

import com.halflife3.Mechanics.Vector2;
import javafx.scene.transform.Affine;

public abstract class Controllable extends Sprite {
	protected String    ipOfClient;
	protected Affine    affine;
	protected short 	degrees;
	protected Vector2 spawnPosition;

	public Controllable(Vector2 position, Vector2 velocity) { super(position, velocity); }

	//region IP getter and setter
	public String getIpOfClient() {	return ipOfClient; }

	public void setIpOfClient(String ipOfClient) { this.ipOfClient = ipOfClient; }
	//endregion

	//region Rotation getter and setter
	public void setAffine(Affine affine) { this.affine = affine; }

	public Affine getAffine() { return affine; }

	public void setDegrees(short degrees) { this.degrees = degrees;}

	public short getDegrees() { return degrees; }
	//endregion

	public Vector2 getSpawnPosition() {
		return spawnPosition;
	}
}
