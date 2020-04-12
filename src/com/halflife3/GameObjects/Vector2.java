package com.halflife3.GameObjects;

import java.io.Serializable;
import java.util.Objects;

/**
 * This class is used in HalfLife to represent a two dimensional vector. It's intended use is to represent positions
 * inside of the game world, as well as velocities of objects (and even accelerations). This class tries to obey the
 * laws that mathematical vectors follow, with methods which mimic the algebra of vectors (addition, subtraction, and
 * scalar multiplication/division).
 * <p>
 * As well as these, this class contains methods which act on vectors too. Notably, this class supports operations such
 * as finding the distance between two vectors (both euclidean and squared, as the squared version avoids a costly
 * square root call in situations where the precise distance isn't needed), magnitude, normalisation, and inversion.
 * <p>
 * Furthermore, this class also implements functions implemented from {@code Object}, such as toString (to give the
 * class a "pretty print" format when debugging), equals (to allow similar testing to Strings), and hashCode (for the
 * ability to use this class in any hash based implementations). These are convenience methods which will make the job
 * of writing code involving this method much easier to do.
 * <p>
 * As well as these, this class supports a deep copy constructor. This allows for duplicates of a given vector to be
 * made and avoid issues with Java's use of references when it can use them.
 */
public class Vector2 implements Serializable {
	/** A variable used for the Serialization interface, hopefully be made redundant in the future */
	private static final long   serialVersionUID = 4L;
	/** The x and y components of this {@code Vector2} */
	private              double x, y;

	//region Constructors

	/** The default constructor, makes a vector with elements {@code x == 0 & y == 0} */
	public Vector2() {}

	/** A constructor which takes two doubles, an x and a y, and sets the respective components of the vector
	 *
	 * @param x The value to set as the {@code x} component
	 * @param y The value to set as the {@code y} component*/
	public Vector2(double x, double y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * The deep copy constructor, uses another Vector2 to get the values for the x and y components and then generates
	 * a completely new object with these values
	 *
	 * @param toCopy The {@code Vector2} to deep copy
	 */
	public Vector2(Vector2 toCopy) {
		x = toCopy.getX();
		y = toCopy.getY();
	}
	//endregion

	//region Vector Algebra

	/**
	 * A method to add a vector to the current vector
	 *
	 * @param v The vector to add to the current vector
	 *
	 * @return The sum of the current and passed vector
	 */
	public Vector2 add(Vector2 v) {
		x += v.x;
		y += v.y;
		return this;
	}

	/**
	 * A method to add two values to the components of the current vector
	 *
	 * @param x The value to add to the x component of the current vector
	 * @param y The value to add to the y component of the current vector
	 *
	 * @return The vector with the values added to the corresponding components
	 */
	public Vector2 add(double x, double y) {
		this.x += x;
		this.y += y;
		return this;
	}

	/**
	 * A method to subtract a vector from the current vector
	 *
	 * @param v The vector to subtract from the current one
	 *
	 * @return The difference between the current and passed vector
	 */
	public Vector2 subtract(Vector2 v) {
		x -= v.x;
		y -= v.y;
		return this;
	}

	/**
	 * A method to subtract two values from the components of the current vector
	 *
	 * @param x The value to subtract from the x component of the current vector
	 * @param y The value to subtract from the y component of the current vector
	 *
	 * @return The vector with the values subtracted from the corresponding components
	 */
	public Vector2 subtract(double x, double y) {
		this.x -= x;
		this.y -= y;
		return this;
	}

	/**
	 * A method to multiply a vector by a scalar
	 *
	 * @param s The scalar to multiply the vector by
	 *
	 * @return The scalar multiplication of the current vector and the passed scalar
	 */
	public Vector2 multiply(double s) {
		x *= s;
		y *= s;
		return this;
	}

	/**
	 * A method to divide the current vector by a scalar
	 *
	 * @param s The scalar to divide the vector by
	 *
	 * @return The scalar division of the current vector and the passed scalar
	 */
	public Vector2 divide(double s) {
		x /= s;
		y /= s;
		return this;
	}
	//endregion

	//region Distance

	/**
	 * A method to return the euclidean distance between the current vector and the passed vector
	 *
	 * @param v The vector to check the distance to
	 *
	 * @return A double representing the euclidean distance between the current and passed vector
	 */
	public double distance(Vector2 v) {
		return Math.sqrt(Math.pow(v.x - x, 2) + Math.pow(v.y - y, 2));
	}

	/**
	 * A static method to return the euclidean distance between two passed vectors
	 *
	 * @param a One of the vectors to calculate the distance between
	 * @param b The second vector to calculate the distance between
	 *
	 * @return The euclidean distance between the two passed vectors
	 */
	public static double Distance(Vector2 a, Vector2 b) {
		return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
	}

	/**
	 * A method to return the distance squared between two points. Efficient as it doesn't require a costly square root call
	 *
	 * @param v The Vector2 to find the squared distance to
	 *
	 * @return The squared distance between the two points
	 */
	public double squareDistance(Vector2 v) {
		return Math.pow(v.x - x, 2) + Math.pow(v.y - y, 2);
	}

	/**
	 * A static method to return the square euclidean distance between the two passed vectors
	 *
	 * @param a The first vector to calculate the distance between
	 * @param b The second vector to calculate the distance between
	 *
	 * @return The squared distance between the two given vectors
	 */
	public static double SquareDistance(Vector2 a, Vector2 b) {
		return Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2);
	}
	//endregion

	//region Misc operations

	/**
	 * A method to calculate the magnitude of the vector
	 *
	 * @return The magnitude of the given vector
	 */
	public double magnitude() { return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)); }

	/**
	 * A static method to calculate the magnitude of the given vector
	 *
	 * @param v The vector to calculate the magnitude of
	 *
	 * @return The magnitude of the given {@code Vector2}
	 */
	public double Magnitude(Vector2 v) {return Math.sqrt(Math.pow(v.x, 2) + Math.pow(v.y, 2));}

	/**
	 * A method to normalise the current {@code Vector2}
	 *
	 * @return The normalised value of the current {@code Vector2}
	 */
	public Vector2 normalise() {
		double mag = magnitude();
		x /= mag;
		y /= mag;
		return this;
	}

	/**
	 * A static method to normalise the given {@code Vector2}
	 *
	 * @param v The vector to normalise
	 *
	 * @return The normalised form of the given {@code Vector2}
	 */
	public static Vector2 Normalise(Vector2 v) {
		double mag = v.magnitude();
		v.x /= mag;
		v.y /= mag;
		return v;
	}

	/**
	 * A method to reverse the direction of the current vector
	 *
	 * @return A {@code Vector2} representing this object, but reversed
	 */
	public Vector2 reverse() {
		x = -x;
		y = -y;
		return this;
	}
	//endregion

	//region Getters and Setters for X and Y

	/**
	 * A method to retrieve the x value of the current {@code Vector2}
	 *
	 * @return A double representing the x value of this {@code Vector2}
	 */
	public double getX() {
		return x;
	}

	/**
	 * A method to retrieve the y value of the current {@code Vector2}
	 *
	 * @return A double representing the y value of this {@code Vector2}
	 */
	public double getY() {
		return y;
	}

	/**
	 * A method to set the x value of the current {@code Vector2}
	 *
	 * @param x The x value to set the current x component to
	 */
	public void setX(double x) {
		this.x = x;
	}

	/**
	 * A method to retrieve the x value of the current {@code Vector2}
	 *
	 * @param y The y value to set the current y component to
	 */
	public void setY(double y) {
		this.y = y;
	}

	/**
	 * A method to set both x and y at the same time
	 *
	 * @param x The x value to set
	 * @param y The y value to set
	 */
	public void setXY(double x, double y) {
		this.x = x;
		this.y = y;
	}

	/** A method to reset the current {@code Vector2} */
	public void reset() {
		x = 0;
		y = 0;
	}
	//endregion

	//region Overrides For Object Methods (toString, Equals, hashCode)
	/**
	 * The toString implementation for this class, acts as a "pretty printer" for the information this object contains
	 *
	 * @return The formatted version of the data contained in this class, as if it was a 2D point in maths (i.e. (x, y))
	 */
	@Override
	public String toString() { return "(" + x + ", " + y + ")"; }

	/**
	 * The equals implementation of this class so that we can test for equality. The object is first checked to make
	 * sure that it has a type of {@code Vector2} and then the individual components are compared against each other
	 *
	 * @param obj The object to check is equal this {@code Vector2} or not
	 *
	 * @return True if the object has type {@code Vector2} and the components are the same, false otherwise
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Vector2)
			return ((Vector2) obj).x == x && ((Vector2) obj).y == y;
		return false;
	}

	/**
	 * The custom implementation of the hashCode function for use in any hash based data structure. The x and y
	 * components are passed to {@code Objects.hash()} to generate the hash code for a given set of components
	 *
	 * @return The hash code of this object based on it's components
	 */
	@Override
	public int hashCode() { return Objects.hash(x, y); }
	//endregion
}