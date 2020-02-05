package com.halflife3.Model;

/** A class to represent a two dimensional position in space */
public class Vector2 {
    /** Variables to hold the position variables, stored as doubles */
    private double x, y;

    /** A constant value used to represent a rightwards vector in the global frame */
    public final Vector2 RIGHT = new Vector2(1, 0);
    /** A constant value used to represent a leftwards vector in the global frame */
    public final Vector2 LEFT = new Vector2(-1, 0);
    /** A constant value used to represent a downwards vector in the global frame */
    public final Vector2 DOWN = new Vector2(0, -1);
    /** A constant value used to represent an upwards vector in the global frame */
    public final Vector2 UP = new Vector2(0, 1);

    /** A base constructor for the Vector2 class to initialise a generic instance*/
    public Vector2() {}

    /**
     * Constructor to create a Vector2
     * @param x The x position of the point
     * @param y The y position of the point
     */
    public Vector2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * A method to add a vector to the current vector
     * @param v The vector to add to the current vector
     * @return The sum of the current and passed vector
     */
    public Vector2 add(Vector2 v) {
        this.x += v.x;
        this.y += v.y;
        return this;
    }

    /**
     * A method to subtract a vector from the current vector
     * @param v The vector to subtract from the current one
     * @return The difference between the current and passed vector
     */
    public Vector2 subtract(Vector2 v) {
        this.x -= v.x;
        this.y -= v.y;
        return this;
    }

    /**
     * A method to multiply a vector by a scalar
     * @param s The scalar to multiply the vector by
     * @return The scalar multiplication of the current vector and the passed scalar
     */
    public Vector2 multiply(double s) {
        this.x *= s;
        this.y *= s;
        return this;
    }

    /**
     * A method to divide the current vector by a scalar
     * @param s The scalar to divide the vector by
     * @return The scalar division of the current vector and the passed scalar
     */
    public Vector2 divide(double s) {
        this.x /= s;
        this.y /= s;
        return this;
    }

    /**
     * A method to return the euclidean distance between the current vector and the passed vector
     * @param v The vector to check the distance to
     * @return A double representing the euclidean distance between the current and passed vector
     */
    public double distance(Vector2 v) {
        return Math.sqrt(Math.pow(v.x - this.x, 2) + Math.pow(v.y - this.y, 2));
    }

    /**
     * A method to return the distance squared between two points. Efficient as it doesn't require a costly square root call
     * @param v The Vector2 to find the squared distance to
     * @return The squared distance between the two points
     */
    public double squareDistance(Vector2 v) { return Math.pow(v.x - this.x, 2) + Math.pow(v.y - this.y, 2);}

    public double getX() { return this.x; }
    public double getY() { return this.y; }
}
