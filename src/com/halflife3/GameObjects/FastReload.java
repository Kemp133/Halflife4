package com.halflife3.GameObjects;

import com.halflife3.View.Camera;
import javafx.scene.canvas.*;
import javafx.scene.shape.*;

public class FastReload extends Sprite {
	public Circle circle;

	public FastReload(Vector2 position) {
		super(position, new Vector2());
		keys.add("reload");
		setSprite("res/Sprites/Power-ups/damageBoost.png");
		circle = new Circle(position.getX() + getWidth() / 2 + 1, position.getY() + getHeight() / 2 + 1,
				Math.max(getWidth(), getHeight()) / 2 + 1);
	}

	@Override
	public void render(GraphicsContext gc) {
		gc.drawImage(sprite, position.getX() - Camera.GetOffset().getX(), position.getY() - Camera.GetOffset().getY());
	}

	@Override
	public Circle getBounds() {
		return circle;
	}

	@Override
	public void update(double time) {

	}
}
