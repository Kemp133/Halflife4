package com.halflife3.Model;

import com.halflife3.Controller.Input;
import com.halflife3.Controller.ObjectManager;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;

import java.io.FileInputStream;

public class Crosshair extends GameObject {

    Image sprite;
    Input input;

    final int SPRITE_SIZE = 32;

    public Crosshair(Vector2 position, Vector2 velocity, short rotation, ObjectManager om, Input input,int ID) {
        super(position, velocity, rotation, om,ID);
        this.input = input;
    }

    {
        try {
            FileInputStream inputted = new FileInputStream("res/crosshair.png");
            sprite = new Image(inputted);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public Rectangle GetBounds() {
        return null;
    }

    @Override
    public boolean intersects(GameObject s) {
        return false;
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.drawImage(sprite, position.getX() - (float)(SPRITE_SIZE / 2), position.getY() - (float)(SPRITE_SIZE / 2));
    }

    @Override
    public void update(double time) {
        position.setX(input.mousePosition.getX());
        position.setY(input.mousePosition.getY());
    }
}
