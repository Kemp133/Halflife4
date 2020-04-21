package com.halflife3.GameObjects;

import com.halflife3.Networking.Packets.PositionPacket;
import com.halflife3.View.Camera;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Circle;

/**
 * This class represents the ball in the main game mode. This class extends sprite, adding some required methods and
 * fields to implement the logic of the ball. This is in the form of a circle to act as a collider, a {@code Vector2}
 * to say where the original position of the ball was, a boolean to say whether the ball is held or not, and a
 * Vector2 to say where on the map it should spawn
 */
public class Ball extends Sprite {
	public  Circle         circle;
	public  Vector2        originalPosition;
	public  boolean        isHeld = false;
	private Vector2        spawnPosition;
	private PositionPacket positionPacket;

	/**
	 * The default constructor which mirrors the super constructor. Super is called, then a key with the value of
	 * "Ball" is added, and it's position is put to the center of the ball. The spawn point is set to the passed
	 * position, and the circle shape is initialized
	 */
	public Ball(Vector2 position) {
		super(position, new Vector2(), "Sprites/Ball/Ball.png");
		keys.add("Ball");
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

	/**
	 * This method is called when the ball collides with the walls to decide how the ball's velocity should be changed
	 *
	 * @param bounce The operation to be carried out. 1 - means the ball is coming from the side of the wall,
	 *               therefore the X axis' velocity needs to be reversed. 2 - means the ball is coming from either the
	 *               top or the bottom of the wall and therefore the Y axis' velocity needs changing
	 */
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
		}

		circle.setCenterX(position.getX() + getWidth() / 2);
		circle.setCenterY(position.getY() + getHeight() / 2);
	}

	/** This method resets the ball to its original values */
	public void reset() {
		isHeld   = false;
		position = spawnPosition;
		circle.setCenterX(position.getX() + getWidth() / 2);
		circle.setCenterY(position.getY() + getHeight() / 2);
		originalPosition = spawnPosition;
		resetVelocity();
	}

	/**
	 * Getter method for the PositionPacket of the ball
	 *
	 * @return PositionPacket with the information about the ball's position, velocity and if it is being held
	 */
	public PositionPacket getPositionPacket() {
		positionPacket.posX      = getPosX();
		positionPacket.posY      = getPosY();
		positionPacket.velX      = getVelX();
		positionPacket.velY      = getVelY();
		positionPacket.holdsBall = isHeld;
		return positionPacket;
	}
}
