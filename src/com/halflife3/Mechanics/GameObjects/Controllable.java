package com.halflife3.Mechanics.GameObjects;

import com.halflife3.Mechanics.Vector2;
import javafx.scene.transform.Affine;

public abstract class Controllable extends Sprite {
	protected String        ipOfClient;
	protected Affine        affine;
	protected short         rotation;
	protected Vector2 		spawn_point;
	protected boolean       AI;

	public Controllable(Vector2 position, Vector2 velocity) { super(position, velocity); }

	//region IP getter and setter
	public String getIpOfClient() {	return ipOfClient; }

	public void setIpOfClient(String ipOfClient) { this.ipOfClient = ipOfClient; }
	//endregion

	//region Rotation getter and setter
	public void setAffine(Affine affine) { this.affine = affine; }

	public Affine getAffine() { return affine; }

	public void setRotation(short rotation) { this.rotation = rotation;}

	public short getRotation() { return rotation; }
	//endregion

	//region SpawnPoint getter and setter
	public Vector2 getSpawn_point() { return spawn_point; }

	public void setSpawn_point(Vector2 spawn_point) { this.spawn_point = spawn_point; }
	//endregion

	//region AI getter and setter
	public boolean isAI() { return AI; }

	public void setAI(boolean AI) { this.AI = AI; }
	//endregion

}
