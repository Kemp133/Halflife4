package com.halflife3.GameObjects;

import com.halflife3.Mechanics.Vector2;
import com.halflife3.View.Camera;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Circle;

public class Bullet extends Sprite {
    private Circle circle;
    private String shooterName;

    public Bullet(Vector2 position, Vector2 velocity, String shooterName) {
        super(position, velocity);
        keys.add("Bullet");
        this.shooterName = shooterName;
        circle = new Circle(position.getX() + getWidth() / 2,
                position.getY() + getHeight() / 2,
                Math.max(getWidth(), getHeight()) / 2 + 1);
        setSprite("res/bullet.png");
    }

    @Override
    public Circle getBounds() {
        return circle;
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.drawImage(sprite, getPosX() - Camera.GetOffset().getX(), getPosY() - Camera.GetOffset().getY());
    }

    @Override
    public void update(double time) {
        position = position.add(new Vector2(velocity).multiply(time));
        circle.setCenterX(position.getX() + getWidth() / 2);
        circle.setCenterY(position.getY() + getHeight() / 2);
    }

    public String getShooterName() {
        return shooterName;
    }
}