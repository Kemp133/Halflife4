package com.halflife3.GameObjects;

import com.halflife3.View.Camera;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Circle;

public class Bullet extends Sprite {
	private final Circle circle;
	private final String shooterName;

	public Bullet(Vector2 position, Vector2 velocity, String shooter) {
		super(position, velocity);
		keys.add("Bullet");
		shooterName = shooter;
		circle      = new Circle(position.getX() + getWidth() / 2, position.getY() + getHeight() / 2,
				Math.max(getWidth(), getHeight()) / 2 + 1);
		setSprite("res/Sprites/Bullets/bullet.png");
	}

	@Override
	public Circle getBounds() {
		return circle;
	}

	@Override
	public void render(GraphicsContext gc) {
		gc.drawImage(sprite, getPosX() - Camera.GetOffset().getX(), getPosY() - Camera.GetOffset().getY());
	}

	@Override
	public void update(double time) {
		position.add(new Vector2(velocity).multiply(time));
		circle.setCenterX(position.getX() + getWidth() / 2);
		circle.setCenterY(position.getY() + getHeight() / 2);
	}

	public String getShooterName() {
		return shooterName;
	}
}
