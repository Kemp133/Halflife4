package com.halflife3.Model;

public abstract class GameObject {
    //Using a Vector2 to represent the location of a game object in the world
    private Vector2 position;
    //Using a short as a large number isn't needed to represent the rotation of the object
    private short rotation = 0;
}
