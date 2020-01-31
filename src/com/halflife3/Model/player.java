package com.halflife3.Model;

import javafx.scene.shape.Rectangle;


public class player extends GameObject{
    public double width = 49;
    public double height = 43;

    //Initialize a player
    public player(Vector2 position, Vector2 velocity, short rotation){
        super(position,velocity,rotation,Role.player);
    }

    @Override
    public Rectangle GetBounds() {
        //Create rectangle object(one of node in javafx)
        Rectangle rectangle = new Rectangle(this.position.getX(), this.position.getY(), this.width, this.height);
        return rectangle;
    }

    //update the position
    @Override
    public void update() {

    }

}