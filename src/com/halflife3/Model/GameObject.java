package com.halflife3.Model;

public abstract class GameObject implements IRenderable, IUpdateable{
    //Using a Vector2 to represent the location of a game object in the world
    protected Vector2 position;
    //Using a Vector2 to represent the current velocity of the object
    protected Vector2 velocity;
    //Using a short as a large number isn't needed to represent the rotation of the object
    protected short rotation;

    public GameObject(Vector2 position, Vector2 velocity, short rotation) {
        this.position = position;
        this.velocity = velocity;
        this.rotation = rotation;
    }
}
