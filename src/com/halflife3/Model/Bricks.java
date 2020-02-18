/*
package com.halflife3.Model;

import com.halflife3.Controller.ObjectManager;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Bricks extends GameObject {
    public double width = 40;
    public double height = 40;
    public Rectangle rectangle = new Rectangle(position.getX(), position.getY(), width, height);
    private Image image;

    public Bricks(Vector2 position, Vector2 velocity, short rotation, ObjectManager om) {
        super(position,velocity,rotation, om);
    }

    @Override
    public Rectangle GetBounds() {
        return rectangle;
    }

    @Override
    public boolean intersects(GameObject s) {
        return false;
    }

    public void setImage(String file) throws FileNotFoundException {
        FileInputStream pngFile = new FileInputStream(file);
        image = new Image(pngFile);
        width = image.getWidth();
        height = image.getHeight();
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.drawImage(image, position.getX(), position.getY());
    }

    @Override
    public void update(double time) {
        //TODO: If bullet touches the brick and the brick can be destroyed, remove brick
    }
}
*/
