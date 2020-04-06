package com.halflife3.Mechanics.PowerUps;

import com.halflife3.GameObjects.Sprite;
import com.halflife3.Mechanics.Vector2;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Shape;

public abstract class BasePowerUp extends Sprite {

	public BasePowerUp(Vector2 position, Vector2 velocity) {
		super(position, velocity);
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