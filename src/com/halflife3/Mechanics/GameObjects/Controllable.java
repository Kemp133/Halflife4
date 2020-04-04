package com.halflife3.Mechanics.GameObjects;

import com.halflife3.Mechanics.Vector2;
import javafx.scene.transform.Affine;

/**
 * This class is used to represent a controllable class. This can be either a human controller player, or a player which
 * is controlled by the computer instead. This extends on top of Sprite with four new attributes: An IP, an affine
 * value, a rotation value, and a spawn point. These are as below:
 *
 * <ul>
 *     <li>{@code ipOfClient} - Represents the IP of this player. Stored as a {@code String}</li>
 *     <li>{@code affine} - Represents a transformation of space while preserving the straightness of lines, used for
 *     rotating the object on the {@code GraphicsContext}. Stored as a {@code Affine}. @see Affine</li>
 *     <li>{@code degrees} - Represents a serializable friendly variable to transfer the rotation of this object over
 *     the network. Stored as a {@code short}</li>
 *     <li>{@code spawnPosition} - Represents where this player will spawn. Stored as a {@code Vector2}</li>
 * </ul>
 */
public abstract class Controllable extends Sprite {
	protected String  ipOfClient;
	protected Affine  affine;
	protected short   degrees;
	protected Vector2 spawnPosition;

	/**
	 * A constructor taking two {@code Vector2}s, one for the position and one for the velocity of the object. This
	 * constructor also sets the spawn point variable of this class to the passed position
	 * @param position The position of the object
	 * @param velocity The velocity of the object
	 */
	public Controllable(Vector2 position, Vector2 velocity) {
		super(position, velocity);
		spawnPosition = position;
	}

	//region IP getter and setter
	/**
	 * A method to get the IP of this object
	 *
	 * @return A string representing the IP of this object
	 */
	public String getIpOfClient() { return ipOfClient; }

	/**
	 * A method to set the IP of this object
	 *
	 * @param ipOfClient A string representing the IP of this object
	 */
	public void setIpOfClient(String ipOfClient) { this.ipOfClient = ipOfClient; }
	//endregion

	//region Rotation getter and setter
	/** A method to set the {@code affine} variable
	 *
	 * @param affine The new {@code affine} variable to set
	 */
	public void setAffine(Affine affine) { this.affine = affine; }
	/** A method to get the {@code affine} variable
	 *
	 * @return The affine in the {@code affine} variable
	 */
	public Affine getAffine()             { return affine; }
	/** A method to get the {@code degrees} short that this class stores
	 *
	 * @return The short value for the degrees of this rotation
	 */
	public short getDegrees()             { return degrees; }
	/** A method to set the {@code degrees} variable
	 *
	 * @param degrees The short to set the {@code degrees} variable too
	 */
	public void setDegrees(short degrees) { this.degrees = degrees;}
	//endregion
}
