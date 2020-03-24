package com.halflife3.Model.ball;

import com.halflife3.Model.Sprite;
import com.halflife3.Model.Vector2;
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
        circle = new Circle(position.getX() + 12, position.getY() + 12, 12);
    }


    @Override
    public void render(GraphicsContext gc) {
        gc.drawImage(sprite, getPosX() - Camera.GetOffset().getX(), getPosY() - Camera.GetOffset().getY());
    }

    @Override
    public void update(double time) {
        original_position = new Vector2(position);
        if (velocity.getX()*(velocity.getX() - acc.getX()) > 0)
            velocity.setX(velocity.getX() - acc.getX());
        else
            velocity.setX(0);
        if (velocity.getY()*(velocity.getY() - acc.getY()) > 0)
            velocity.setY(velocity.getY() - acc.getY());
        else
            velocity.setY(0);
        position = position.add(new Vector2(velocity).multiply(time));
        circle.setCenterX(position.getX() + 12);
        circle.setCenterY(position.getY() + 12);
    }

    public void collision(int bounce, double time) {

        if (bounce == 1) {
            this.position = original_position;
            velocity.setX(-velocity.getX());
            acc.setX(-acc.getX());
        }
        if(bounce == 2){
            this.position = original_position;
            velocity.setY(-velocity.getY());
            acc.setY(-acc.getY());
        }
        if(bounce == 3){
            this.position = original_position;
            velocity = new Vector2(0,0);
            acc= new Vector2(0,0);
        }
        circle.setCenterX(position.getX() + 12);
        circle.setCenterY(position.getY() + 12);
    }

}
