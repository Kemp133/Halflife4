package com.halflife3.Model;

import com.halflife3.Controller.ObjectManager;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;

import java.io.FileInputStream;

public class Bullet extends GameObject {
    private Image sprite;

    public Bullet(Vector2 position, Vector2 velocity, short rotation, ObjectManager om) {
        super(position, velocity, rotation, om);
        keys.add("Bullet");
        try {
            FileInputStream bulletPNG = new FileInputStream("res/bullet.png");
            sprite = new Image(bulletPNG);
        } catch (Exception e) {
            System.out.println("Error loading image");
        }
    }

    @Override
    public Rectangle GetBounds() {
        return new Rectangle(position.getX(), position.getY(), sprite.getWidth(), sprite.getHeight());
    }

    @Override
    public boolean intersects(GameObject s) {
        return false;
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.drawImage(sprite, getX(), getY());
    }

    @Override
    public void update(double time) {
        position = position.add(new Vector2(velocity).multiply(time));
    }
}
