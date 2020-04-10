package com.halflife3.GameObjects;

import com.halflife3.Controller.ClientController;
import com.halflife3.Mechanics.Vector2;
import com.halflife3.View.Camera;
import javafx.scene.canvas.*;
import javafx.scene.image.*;
import javafx.scene.transform.*;

import java.io.FileInputStream;
import java.io.IOException;

public class Player extends Controllable {
	//region Variables
	private float  RELOAD_TIME;
	public  float  reload;
	private int    movementSpeed;
	private Image  sprite2;
	private Affine affine;
	//endregion

	public Player(Vector2 position) {
		super(position);
		keys.add("player");
		setSprite2("res/Sprites/PlayerSkins/Cosmo_Moving.png");
		affine = new Affine();
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
		if (reload < RELOAD_TIME)
			reload++;
		if (stunned <= ClientController.FPS / 2f && stunned != 0) {
			velocity.setX(0);
			velocity.setY(0);
			deceleration.setX(0);
			deceleration.setY(0);
			stunned--;
		} else {
			orgPos = new Vector2(position);
			if (velocity.getX() * new Vector2(velocity).subtract(deceleration).getX() > 0)
				velocity.subtract(deceleration);
			position.add(new Vector2(velocity).multiply(time));
			isMoving = !orgPos.equals(position);
			circle.setCenterX(position.getX() + getWidth() / 2 + 1);
			circle.setCenterY(position.getY() + getHeight() / 2 + 1);
			if (stunned > 0)
				stunned--;
		}
	}

	public void setSprite2(String pathToSprite) {
		try (FileInputStream fis = new FileInputStream(pathToSprite)) {
			sprite2 = new Image(fis);
		} catch (IOException e) {
			System.err.println("Image not found!");
		}
	}

	public void collision(Brick block, double time) {
		Vector2 dir = new Vector2(block.getPosX() - orgPos.getX(), block.getPosY() - orgPos.getY());

		if (Math.abs(dir.getX()) < 30 && Math.abs(dir.getY()) < 30) {
			do {
				position.add(new Vector2(velocity).multiply(-time));
				circle.setCenterX(position.getX() + getWidth() / 2 + 1);
				circle.setCenterY(position.getY() + getHeight() / 2 + 1);
			} while (block.getBounds().intersects(circle.getBoundsInLocal()));
			return;
		}

		if (dir.getX() < -30 && dir.getY() > -30 && dir.getY() < 30 && velocity.getX() < 0)
			velocity.setX(0);
		else if (dir.getX() > 30 && dir.getY() > -30 && dir.getY() < 30 && velocity.getX() > 0)
			velocity.setX(0);

		if (dir.getY() > 30 && dir.getX() > -30 && dir.getX() < 30 && velocity.getY() > 0)
			velocity.setY(0);
		else if (dir.getY() < -30 && dir.getX() > -30 && dir.getX() < 30 && velocity.getY() < 0)
			velocity.setY(0);
	}

	public void setAffine(Affine affine) { this.affine = affine; }

	public float getReloadTime() {
		return RELOAD_TIME;
	}

	public void setReloadTime(float RELOAD_TIME) {
		this.RELOAD_TIME = RELOAD_TIME;
	}

	public int getMovementSpeed() {
		return movementSpeed;
	}

	public void setMovementSpeed(int movementSpeed) {
		this.movementSpeed = movementSpeed;
	}
}