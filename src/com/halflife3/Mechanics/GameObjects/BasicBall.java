package com.halflife3.Mechanics.GameObjects;

import com.halflife3.Mechanics.Vector2;
import com.halflife3.View.Camera;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Circle;

public class BasicBall extends Sprite {
    public Circle circle;
    Vector2 original_position;
    public boolean isHeld = false;

    public BasicBall(Vector2 position, Vector2 velocity) {
        super(position, velocity);
        setSprite("res/Sprites/Ball/Ball.png");
        keys.add("Ball");
        position.subtract(getWidth() / 2, getHeight() / 2);
        circle = new Circle(position.getX(),
                position.getY(),
                Math.max(getWidth(), getHeight()) / 2);
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.drawImage(sprite, getPosX() - Camera.GetOffsetX(), getPosY() - Camera.GetOffsetY());
    }

    @Override
    public void update(double time) {
        original_position = new Vector2(position);
        double velX = velocity.getX() - acceleration.getX();
        double velY = velocity.getY() - acceleration.getY();

        velocity.setX((velocity.getX() * velX > 0) ? velX : 0);
        velocity.setY((velocity.getY() * velY > 0) ? velY : 0);

        float accInc = 0.5f;
        acceleration.setX(acceleration.getX() < 0 ? acceleration.getX() - accInc : acceleration.getX() + accInc);
        acceleration.setY(acceleration.getY() < 0 ? acceleration.getY() - accInc : acceleration.getY() + accInc);

        position.add(new Vector2(velocity).multiply(time));
        circle.setCenterX(position.getX() + getWidth() / 2);
        circle.setCenterY(position.getY() + getHeight() / 2);
    }

    public void collision(int bounce) {
        switch (bounce) {
            case 1 : {
                position = original_position;
                velocity.setX(-velocity.getX());
                acceleration.setX(-acceleration.getX());
                break;
            }

            case 2 : {
                position = original_position;
                velocity.setY(-velocity.getY());
                acceleration.setY(-acceleration.getY());
                break;
            }

            case 3 : {
                position = original_position;
                velocity.setX(0);
                velocity.setY(0);
                acceleration.setX(0);
                acceleration.setY(0);
                break;
            }
        }

        circle.setCenterX(position.getX() + getWidth() / 2);
        circle.setCenterY(position.getY() + getHeight() / 2);
    }

}
