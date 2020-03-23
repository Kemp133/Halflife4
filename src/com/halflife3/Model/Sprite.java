package com.halflife3.Model;

import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

import java.io.*;

public abstract class Sprite extends GameObject {
	public Image sprite;

	public Sprite(Vector2 position, Vector2 velocity) {
		super(position, velocity);
	}

	public void setSprite(String pathToSprite) {
		try(FileInputStream fis = new FileInputStream(pathToSprite)) {
			sprite = new Image(fis);
		} catch (IOException e) {
			System.err.println("Image not found!");
		}
	}

	public Image getSprite() { return sprite; }

	public Shape getBounds() {
		return new Rectangle(position.getX(),position.getY(),getWidth(),getHeight());
	}

	public double getWidth() { return (sprite == null) ? 0 : sprite.getWidth();	}

	public double getHeight() { return (sprite == null) ? 0 : sprite.getHeight(); }
}
