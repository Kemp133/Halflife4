package com.halflife3.Model.ball;


import com.halflife3.Controller.ObjectManager;
import com.halflife3.Model.GameObject;
import com.halflife3.Model.Vector2;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Shape;

public class BasicBall extends GameObject{
    public BasicBall(Vector2 position, Vector2 velocity, double rotation, ObjectManager om) {
        super(position, velocity, rotation, om);
    }

    @Override
    public Shape GetBounds() {
        return null;
    }

    @Override
    public boolean intersects(GameObject s) {
        return false;
    }

    @Override
    public void render(GraphicsContext gc, Vector2 offset) {

    }

    @Override
    public void update(double time) {

    }
}
