package com.halflife3.GameObjects;

import com.halflife3.Controller.ObjectManager;
import com.halflife3.GameObjects.Interfaces.ICollidable;
import com.halflife3.GameObjects.Interfaces.IRenderable;
import com.halflife3.GameObjects.Interfaces.IUpdateable;

import java.util.HashSet;
import java.util.Iterator;

public abstract class GameObject implements IRenderable, IUpdateable, ICollidable {
	//region Class Fields
	protected Vector2         position;
	protected Vector2         velocity;
	protected Vector2         deceleration;
	/**
	 * Using a HashSet to store a list of keys (e.g. indicate what type the GameObject is). HashSet for speed, and
	 * to disallow duplicate keys from being added
	 */
	protected HashSet<String> keys;
	//endregion

	//region Constructors
	public GameObject(Vector2 position, Vector2 velocity) {
		this.position = position;
		this.velocity = velocity;
		ObjectManager.addObject(this);
		deceleration = new Vector2(0, 0);
	}

	//An initialiser block
	{
		position = new Vector2();
		velocity = new Vector2();
		keys     = new HashSet<>();
	}
	//endregion

	//region Position getters and Setters
	public double getPosX() {
		return position.getX();
	}

	public double getPosY() {
		return position.getY();
	}

	public Vector2 getPosition() {
		return new Vector2(position);
	}

	public void setPosition(Vector2 position) {
		this.position = position;
	}

	public void setPosition(double x, double y) {
		position.setX(x);
		position.setY(y);
	}
	//endregion

	//region Velocity Getters and Setters
	public double getVelX() {
		return velocity.getX();
	}

	public double getVelY() {
		return velocity.getY();
	}

	public Vector2 getVelocity() {
		return new Vector2(velocity);
	}

	public void setVelX(double x) {
		velocity.setX(x);
	}

	public void setVelY(double y) {
		velocity.setY(y);
	}

	public void setVelocity(Vector2 velocity) {
		this.velocity = velocity;
	}

	public void setVelocity(double x, double y) {
		velocity.setX(x);
		velocity.setY(y);
	}

	public void resetVelocity() {
		velocity.reset();
	}
	//endregion

	//region Get Distance (reference and static variables)

	/**
	 * Get the distance between this GameObject and the given GameObject
	 *
	 * @param entity The GameObject to calculate the distance to
	 *
	 * @return The euclidean distance between this object and the passed object
	 */
	public double getDistance(GameObject entity) {
		return position.distance(entity.getPosition());
	}

	/**
	 * A static method to return the distance between two GameObjects
	 *
	 * @param a The first GameObject to calculate the distance between
	 * @param b The second GameObject to calculate the distance between
	 *
	 * @return The euclidean distance between the two GameObjects
	 */
	public static double GetDistance(GameObject a, GameObject b) { return Vector2.Distance(a.position, b.position); }

	/**
	 * As getDistance, but without a square root call. Gets the square euclidean distance between this GameObject and
	 * the passed GameObject
	 *
	 * @param entity The GameObject to calculate the square distance to
	 *
	 * @return The square euclidean distance between this object and the passed object
	 */
	public double getSquareDistance(GameObject entity) {
		return position.squareDistance(entity.getPosition());
	}

	/**
	 * A static method to return the square distance between two GameObjects
	 *
	 * @param a The first GameObject to calculate the distance between
	 * @param b The second GameObject to calculate the distance between
	 *
	 * @return The square distance between the two passed GameObjects
	 */
	public static double GetSquareDistance(GameObject a, GameObject b) {
		return Vector2.SquareDistance(a.position, a.position);
	}
	//endregion

	//region Destroy (reference and static)

	/** A method to remove this object from the object manager and clear up it's resources */
	public void destroy() {
		ObjectManager.removeObject(this);
		position = null;
		velocity = null;
		keys     = null;
	}

	/**
	 * A static method to remove a given GameObject from the ObjectManager as well as cleaning up the resources
	 * this GameObject uses
	 *
	 * @param toDestroy the GameObject to destroy
	 */
	public static void Destroy(GameObject toDestroy) {
		ObjectManager.removeObject(toDestroy);
		toDestroy.position = null;
		toDestroy.velocity = null;
	}
	//endregion

	//region Keys getter, setter, and containsKey
	public HashSet<String> getKeys() { return keys; }

	public boolean containsKey(String key) {
		return keys.contains(key);
	}
	//endregion

	//region Overridden methods
	@Override
	public String toString() {
		var sb = new StringBuilder();
		sb.append("Position = ");
		sb.append(position.toString());
		sb.append(" | Velocity = ");
		sb.append(velocity.toString());
		sb.append("\nKeys: [");

		Iterator<String> values = keys.iterator();

		if (values.hasNext())
			sb.append(values.next());
		while (values.hasNext()) {
			sb.append(", ");
			sb.append(values.next());
		}

		sb.append("]");
		return sb.toString(); //The prettiest printer to ever exist!
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof GameObject) {
			return position.equals(((GameObject) obj).position) && velocity.equals(((GameObject) obj).velocity) &&
			       keys.equals(((GameObject) obj).keys);
		}
		return false;
	}

	public void setDeceleration(Vector2 deceleration) {
		this.deceleration = deceleration;
	}

	public Vector2 getDeceleration() {
		return deceleration;
	}
	//endregion
}