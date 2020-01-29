package com.halflife3.Model;

import java.util.HashSet;

public abstract class GameObject implements IRenderable, IUpdateable {
    /** Using a Vector2 to represent the location of a game object in the world */
    protected Vector2 position;
    /** Using a Vector2 to represent the current velocity of the object */
    protected Vector2 velocity;
    /** Using a short as a large number isn't needed to represent the rotation of the object */
    protected short rotation;
    /** Using a HashSet to store a list of keys (e.g. indicate what type the GameObject is). HashSet for speed, and
     * to disallow duplicate keys from being added*/
    protected HashSet<String> keys;

    /** A constructor used to initialise a generic instance of this class */
    public GameObject() {}

    /**
     * A constructor used by extending classes to create an instance of the GameObject class
     * @param position The inital position of the GameObject
     * @param velocity The initial velocity of the GameObject
     * @param rotation The initial rotation of the GameObject
     */
    public GameObject(Vector2 position, Vector2 velocity, short rotation) {
        this.position = position;
        this.velocity = velocity;
        this.rotation = rotation;
    }

    //An initialiser block used to set position and velocity to an actual value when creating a generic instance of this class
    {
        position = new Vector2();
        velocity = new Vector2();
    }
}
