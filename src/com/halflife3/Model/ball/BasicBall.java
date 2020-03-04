package com.halflife3.Model.ball;

import com.halflife3.Model.GameObject;
import com.halflife3.Model.Sprite;
import com.halflife3.Model.Vector2;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Shape;

public class BasicBall extends Sprite {
    public BasicBall(Vector2 position, Vector2 velocity, double rotation) {
        super(position, velocity, rotation);
    }

    @Override
    public Shape getBounds() {
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
