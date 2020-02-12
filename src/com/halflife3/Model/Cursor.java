package com.halflife3.Model;

import com.halflife3.Controller.ObjectManager;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Rectangle;

public class Cursor extends GameObject {

    public Cursor(Vector2 position, Vector2 velocity, short rotation, ObjectManager om) {
        super(position, velocity, rotation, om);
    }

    @Override
    public Rectangle GetBounds() {
        return null;
    }

    @Override
    public boolean intersects(GameObject s) {
        return false;
    }

    @Override
    public void render(GraphicsContext gc) {

    }

    @Override
    public void update(double time) {

    }
}
