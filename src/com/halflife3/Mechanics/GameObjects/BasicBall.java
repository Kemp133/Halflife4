package com.halflife3.Mechanics.GameObjects;

import com.halflife3.Mechanics.Vector2;
import com.halflife3.View.Camera;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Circle;

public class BasicBall extends Sprite {
    public Circle circle;
    Vector2 original_position;

    public BasicBall(Vector2 position, Vector2 velocity) {
        super(position, velocity);
        setSprite("res/Sprites/Ball/Ball.png");
        keys.add("Ball");
        circle = new Circle(position.getX() + getWidth() / 2,
                position.getY() + getHeight() / 2,
                Math.max(getWidth(), getHeight()) / 2 + 1);
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.drawImage(sprite, getPosX() - Camera.GetOffsetX(), getPosY() - Camera.GetOffsetY());
    }

    @Override
    public void update(double time) {
        original_position = new Vector2(position);
        double velX = velocity.getX() - acc.getX();
        double velY = velocity.getY() - acc.getY();

        velocity.setX((velocity.getX() * velX > 0) ? velX : 0);
        velocity.setY((velocity.getY() * velY > 0) ? velY : 0);

        position.add(new Vector2(velocity).multiply(time));
        circle.setCenterX(position.getX() + getWidth() / 2);
        circle.setCenterY(position.getY() + getHeight() / 2);
    }

    public void collision(int bounce, double time) {

        switch (bounce) {
            case 1 : {
                position = original_position;
                velocity.setX(-velocity.getX());
                acc.setX(-acc.getX());
            }

            case 2 : {
                position = original_position;
                velocity.setY(-velocity.getY());
                acc.setY(-acc.getY());
            }

            case 3 : {
                position = original_position;
                velocity = new Vector2(0,0);
                acc= new Vector2(0,0);
            }
        }

        circle.setCenterX(position.getX() + getWidth() / 2);
        circle.setCenterY(position.getY() + getHeight() / 2);
    }

}
