package com.halflife3.GameObjects;

import com.halflife3.Controller.GameModes.MainMode;
import com.halflife3.Mechanics.Vector2;
import com.halflife3.Networking.Packets.PositionPacket;
import com.halflife3.View.Camera;
import javafx.scene.canvas.*;
import javafx.scene.image.*;
import javafx.scene.shape.*;
import javafx.scene.transform.*;

import java.io.FileInputStream;
import java.io.IOException;

public class Player extends Controllable {
	//region Variables
	private Affine affine;
	private Image  sprite2;
	public  Circle circle;
	public  float  reload  = MainMode.RELOAD_DURATION;
	public  float  stunned = 0;
	//endregion

	public Player(Vector2 position) {
		super(position);
		keys.add("player");
		setSprite2("res/Sprites/PlayerSkins/Cosmo_Moving.png");
		circle       = new Circle(position.getX() + getWidth() / 2 + 1, position.getY() + getHeight() / 2 + 1,
				Math.max(getWidth(), getHeight()) / 2 + 1);
		affine       = new Affine();
		packetToSend = new PositionPacket();
	}

	@Override
	public Circle getBounds() {
		return circle;
	}

	@Override
	public void render(GraphicsContext gc) {
		double posX = position.getX() - Camera.GetOffset().getX();
		double posY = position.getY() - Camera.GetOffset().getY();

		gc.save();
		gc.setTransform(affine);
		gc.drawImage((isMoving) ? sprite2 : sprite, posX, posY);
		gc.restore();
	}

	@Override
	public void update(double time) {
		if (reload < MainMode.RELOAD_DURATION) reload++;
		if (stunned <= MainMode.STUN_DURATION && stunned != 0) {
			velocity.setX(0);
			velocity.setY(0);
			deceleration.setX(0);
			deceleration.setY(0);
			stunned--;
		} else {
			orgPos = new Vector2(position);
			Vector2 previousVel = new Vector2(velocity);
			if (velocity.getX() * previousVel.subtract(deceleration).getX() > 0) velocity.subtract(deceleration);
			position.add(new Vector2(velocity).multiply(time));
			isMoving = !orgPos.equals(position);
			circle.setCenterX(position.getX() + getWidth() / 2 + 1);
			circle.setCenterY(position.getY() + getHeight() / 2 + 1);
			if (stunned > 0) stunned--;
		}
	}

	public void setSprite2(String pathToSprite) {
		try (FileInputStream fis = new FileInputStream(pathToSprite)) {
			sprite2 = new Image(fis);
		} catch (IOException e) {
			System.err.println("Image not found!");
		}

	}

	public void collision(Bricks block, double time) {
		Vector2 dir = new Vector2(block.getPosX() - orgPos.getX(), block.getPosY() - orgPos.getY());

		if (Math.abs(dir.getX()) < 30 && Math.abs(dir.getY()) < 30) {
			do {
				position.add(new Vector2(velocity).multiply(-time));
				circle.setCenterX(position.getX() + getWidth() / 2 + 1);
				circle.setCenterY(position.getY() + getHeight() / 2 + 1);
			} while (block.getBounds().intersects(circle.getBoundsInLocal()));
			return;
		}

		if (dir.getX() < -30 && dir.getY() > -30 && dir.getY() < 30 && velocity.getX() < 0) velocity.setX(0);
		else if (dir.getX() > 30 && dir.getY() > -30 && dir.getY() < 30 && velocity.getX() > 0) velocity.setX(0);

		if (dir.getY() > 30 && dir.getX() > -30 && dir.getX() < 30 && velocity.getY() > 0) velocity.setY(0);
		else if (dir.getY() < -30 && dir.getX() > -30 && dir.getX() < 30 && velocity.getY() < 0) velocity.setY(0);
	}

	public void reset() {
		stunned    = 0;
		bulletShot = false;
		isMoving   = false;
		holdsBall  = false;
		orgPos     = new Vector2(spawnPosition);
		position   = new Vector2(spawnPosition);
		circle.setCenterX(position.getX() + getWidth() / 2 + 1);
		circle.setCenterY(position.getY() + getHeight() / 2 + 1);
		resetVelocity();
	}

	public void setAffine(Affine affine) { this.affine = affine; }
}