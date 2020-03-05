package com.halflife3.Model.powerUps;

import com.halflife3.Model.GameObject;
import com.halflife3.Model.Sprite;
import com.halflife3.Model.Vector2;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Shape;

public abstract class BasePowerUp extends Sprite {

	public BasePowerUp(Vector2 position, Vector2 velocity, double rotation) {
		super(position, velocity, rotation);
	}

	@Override
	public boolean intersects(GameObject s) {
		return false;
	}

	@Override
	public Shape getBounds() {
		return null;
	}

	@Override
	public void render(GraphicsContext gc) {

	}

	@Override
	public void update(double time) {

	}
}
