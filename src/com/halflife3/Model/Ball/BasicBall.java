package com.halflife3.Model.Ball;

import com.halflife3.Model.Sprite;
import com.halflife3.Model.Vector2;
import javafx.scene.canvas.GraphicsContext;

public class BasicBall extends Sprite {
    public BasicBall(Vector2 position, Vector2 velocity) {
        super(position, velocity);
        setSprite("res/Sprites/Ball.png");
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.drawImage(sprite, getPosX() - (getWidth() / 2), getPosY() - (getHeight() / 2));
    }

    @Override
    public void update(double time) {

    }

}
