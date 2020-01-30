package com.halflife3.Model;

import javafx.scene.shape.Rectangle;


public class player extends GameObject{
    @Override
    public Rectangle GetBounds() {
        //Create rectangle object(one of node in javafx)
        Rectangle rectangle = new Rectangle(this.position.getX(), this.position.getY(), 49, 43);
        return rectangle;
    }

    @Override
    public void update() {

    }
}