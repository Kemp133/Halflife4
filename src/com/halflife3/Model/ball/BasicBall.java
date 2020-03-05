package com.halflife3.Model.ball;

import com.halflife3.Model.Sprite;
import com.halflife3.Model.Vector2;
import javafx.scene.canvas.GraphicsContext;

public class BasicBall extends Sprite {
    public BasicBall(Vector2 position, Vector2 velocity, double rotation) {
        super(position, velocity, rotation);
        setSprite("res/balls/basicBall.png");
    }

    @Override
    public void render(GraphicsContext gc) {

    }

    @Override
    public void update(double time) {

    }

}
