package com.halflife3.Mechanics.GameObjects;

import com.halflife3.Mechanics.Vector2;
import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

import java.io.*;

/**
 * This class deals with any sprites that are used in the game. This is because a lot of objects we had in the game
 * prior to the creation of this class had a lot of duplication between them, meaning that keeping those classes
 * organised was incredibly difficult. This class adds onto GameObject an instance of an {@code Image}, called
 * {@code sprite}. This reference is used to store the sprite of the player, which can be drawn directly onto the
 * {@code GraphicsContext2D} when passed to the render method.
 * <p>
 * This class also provides helper methods to work with this class too, such as a method to set the sprite of the object
 * (both in a constructor and a standalone method), as well as methods to get the bounds of the sprite and it's
 * dimensions.
 *
 * @see Image for more information about how the sprite is represented
 */
public abstract class Sprite extends GameObject {
	/** The {@code Image} used to store the sprite of the game object */
	protected Image sprite;

	/**
	 * The standard constructor that mirrors the super constructor
	 *
	 * @param position The position of the {@code Sprite}
	 * @param velocity The velocity of the {@code Sprite}
	 */
	public Sprite(Vector2 position, Vector2 velocity) {
		super(position, velocity);
	}

	/**
	 * This is a constructor which takes a position and velocity and passes this to the other constuctor, but also takes
	 * a string which is used to call setSprite too so that this doesn't need to be called separately
	 *
	 * @param position      The position of the {@code Sprite}
	 * @param velocity      The velocity of the {@code Sprite}
	 * @param imageLocation The location of the sprite in the folder structure
	 */
	public Sprite(Vector2 position, Vector2 velocity, String imageLocation) {
		this(position, velocity);
		setSprite(imageLocation);
	}

	/**
	 * A method to return the bounds of the sprite as a shape (a class which all JavaFX shapes extend, so can be used to
	 * fine tune the bounds of a given object
	 *
	 * @return A {@code Shape} representing the bounds of the shape
	 */
	public Shape getBounds() {
		return new Rectangle(position.getX(), position.getY(), getWidth(), getHeight());
	}

	/**
	 * A method to set the {@code sprite} attribute of this Sprite
	 *
	 * @param pathToSprite The location of the sprite in the project structure
	 */
	public void setSprite(String pathToSprite) { sprite = new Image(pathToSprite); }

	/**
	 * A method to get the width of the image
	 *
	 * @return The width of the image, as a double
	 */
	public double getWidth() { return (sprite == null) ? 0 : sprite.getWidth(); }

	/**
	 * A method to get the height of the image
	 *
	 * @return The height of the image, as a double
	 */
	public double getHeight() { return (sprite == null) ? 0 : sprite.getHeight(); }
}