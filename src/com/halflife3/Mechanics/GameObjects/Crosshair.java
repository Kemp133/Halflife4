package com.halflife3.Mechanics.GameObjects;

import com.halflife3.Controller.Input;
import com.halflife3.Mechanics.Vector2;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Rectangle;

public class Crosshair extends Sprite {
    public Crosshair(Vector2 position, Vector2 velocity) {
        super(position, velocity);
        setSprite("res/Crosshair.png");
        position.subtract(getWidth() / 2, getHeight() / 2);
    }

    @Override
    public Rectangle getBounds() {
        return null;
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.drawImage(sprite, position.getX() - (float)(getWidth() / 2), position.getY() - (float)(getHeight() / 2));
    }

    @Override
    public void update(double time) {
        position.setX(Input.mousePosition.getX());
        position.setY(Input.mousePosition.getY());
    }
}