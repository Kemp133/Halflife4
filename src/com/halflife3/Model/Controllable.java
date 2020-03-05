package com.halflife3.Model;

import javafx.scene.canvas.GraphicsContext;

public abstract class Controllable extends Sprite {
	protected String ipOfClient;

	public Controllable(Vector2 position, Vector2 velocity, double rotation) {
		super(position, velocity, rotation);
	}

	//region IP getter and setter
	public String getIpOfClient() {
		return ipOfClient;
	}
	public void setIpOfClient(String ipOfClient) {
		this.ipOfClient = ipOfClient;
	}
	//endregion
}
