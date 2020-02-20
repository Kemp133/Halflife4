package com.halflife3.Model;

import com.halflife3.Controller.ObjectManager;
//import com.halflife3.GameUI.AudioForGame;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.shape.Circle;

import java.io.FileInputStream;

public class Bullet extends GameObject {
    private Image sprite;
    private Circle circle = new Circle(position.getX()+4,position.getY()+4, 4);

    public Bullet(Vector2 position, Vector2 velocity, short rotation, ObjectManager om) {
        super(position, velocity, rotation, om);
        keys.add("Bullet");
        try {
            FileInputStream bulletPNG = new FileInputStream("res/bullet.png");
            sprite = new Image(bulletPNG);

//            new AudioForGame().getBullet_music().play();
        } catch (Exception e) {
            System.out.println("Error loading image");
        }
    }


    @Override
    public Circle GetBounds() {
        return circle;
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
        circle.setCenterX(position.getX()+4);
        circle.setCenterY(position.getY()+4);
    }

}
