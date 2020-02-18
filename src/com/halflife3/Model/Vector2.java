package com.halflife3.Model;

import java.io.Serializable;

public class Vector2 implements Serializable {
    private static final long serialVersionUID = 4L;
    private double x, y;

    public Vector2() {}

    public Vector2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2(Vector2 toCopy) {
        x = toCopy.getX();
        y = toCopy.getY();
    }

    /**
     * A method to add a vector to the current vector
     * @param v The vector to add to the current vector
     * @return The sum of the current and passed vector
     */
    public Vector2 add(Vector2 v) {
        x += v.x;
        y += v.y;
        return this;
    }

    /**
     * A method to subtract a vector from the current vector
     * @param v The vector to subtract from the current one
     * @return The difference between the current and passed vector
     */
    public Vector2 subtract(Vector2 v) {
        x -= v.x;
        y -= v.y;
        return this;
    }

    /**
     * A method to multiply a vector by a scalar
     * @param s The scalar to multiply the vector by
     * @return The scalar multiplication of the current vector and the passed scalar
     */
    public Vector2 multiply(double s) {
        x *= s;
        y *= s;
        return this;
    }

    /**
     * A method to divide the current vector by a scalar
     * @param s The scalar to divide the vector by
     * @return The scalar division of the current vector and the passed scalar
     */
    public Vector2 divide(double s) {
        x /= s;
        y /= s;
        return this;
    }

    /**
     * A method to return the euclidean distance between the current vector and the passed vector
     * @param v The vector to check the distance to
     * @return A double representing the euclidean distance between the current and passed vector
     */
    public double distance(Vector2 v) {
        return Math.sqrt(Math.pow(v.x - x, 2) + Math.pow(v.y - y, 2));
    }

    /**
     * A method to return the distance squared between two points. Efficient as it doesn't require a costly square root call
     * @param v The Vector2 to find the squared distance to
     * @return The squared distance between the two points
     */
    public double squareDistance(Vector2 v) {
        return Math.pow(v.x - x, 2) + Math.pow(v.y - y, 2);
    }

    public double magnitude() {
        return Math.sqrt( Math.pow(x, 2) + Math.pow(y, 2) );
    }

    public Vector2 normalise() {
        double mag = magnitude();
        x /= mag;
        y /= mag;
        return this;
    }

    public void reverse(){
        x = -x;
        y = -y;
    }

    public void reset(){
        x = 0;
        y = 0;
    }

    //region Getters and Setters for X and Y
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }
    //endregion

    @Override
    public String toString() {
        return (int)x + "|" + (int)y;
    }
}
