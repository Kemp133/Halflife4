package com.halflife3.Model.ball;

import com.halflife3.Model.GameObject;
import com.halflife3.Model.Sprite;
import com.halflife3.Model.Vector2;
import javafx.scene.canvas.GraphicsContext;

public class BasicBall extends Sprite {
    public BasicBall(Vector2 position, Vector2 velocity) {
        super(position, velocity);
        setSprite("res/balls/basicBall.png");
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
