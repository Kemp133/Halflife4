package com.halflife3.GameObjects;

import com.halflife3.Mechanics.Vector2;
import com.halflife3.Networking.Packets.PositionPacket;
import com.halflife3.View.Camera;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Circle;

public class Ball extends Sprite {
	public  Circle         circle;
	public  Vector2        originalPosition;
	public  boolean        isHeld = false;
	private Vector2        spawnPosition;
	private PositionPacket positionPacket;

	public Ball(Vector2 position, String key) {
		super(position, new Vector2());
		setSprite("res/Sprites/Ball/Ball.png");
		keys.add(key);
		position.subtract(getWidth() / 2, getHeight() / 2);
		spawnPosition         = new Vector2(position);
		circle                = new Circle(position.getX(), position.getY(), Math.max(getWidth(), getHeight()) / 2);
		positionPacket        = new PositionPacket();
		positionPacket.spawnX = spawnPosition.getX();
		positionPacket.spawnY = spawnPosition.getY();
	}

	@Override
	public void render(GraphicsContext gc) {
		gc.drawImage(sprite, getPosX() - Camera.GetOffsetX(), getPosY() - Camera.GetOffsetY());
	}

	@Override
	public void update(double time) {
		originalPosition = new Vector2(position);
		double velX = velocity.getX() - deceleration.getX();
		double velY = velocity.getY() - deceleration.getY();

		velocity.setX((velocity.getX() * velX > 0) ? velX : 0);
		velocity.setY((velocity.getY() * velY > 0) ? velY : 0);

		position.add(new Vector2(velocity).multiply(time));
		circle.setCenterX(position.getX() + getWidth() / 2);
		circle.setCenterY(position.getY() + getHeight() / 2);
	}

	public void collision(int bounce) {
		this.position = originalPosition;
		switch (bounce) {
			case 1: {
				position = originalPosition;
				velocity.setX(-velocity.getX());
				deceleration.setX(-deceleration.getX());
				break;
			}

			case 2: {
				position = originalPosition;
				velocity.setY(-velocity.getY());
				deceleration.setY(-deceleration.getY());
				break;
			}

			case 3: {
				position = originalPosition;
				velocity.setX(0);
				velocity.setY(0);
				deceleration.setX(0);
				deceleration.setY(0);
				break;
			}
		}

		circle.setCenterX(position.getX() + getWidth() / 2);
		circle.setCenterY(position.getY() + getHeight() / 2);
	}

	public void reset() {
		isHeld   = false;
		position = spawnPosition;
		circle.setCenterX(position.getX() + getWidth() / 2);
		circle.setCenterY(position.getY() + getHeight() / 2);
		originalPosition = spawnPosition;
		resetVelocity();
	}

	public PositionPacket getPositionPacket() {
		positionPacket.posX      = getPosX();
		positionPacket.posY      = getPosY();
		positionPacket.velX      = getVelX();
		positionPacket.velY      = getVelY();
		positionPacket.holdsBall = isHeld;
		return positionPacket;
	}
}
