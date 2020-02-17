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

    //Initialize a player
    public Bricks(Vector2 position, Vector2 velocity, short rotation, ObjectManager om){
        super(position,velocity,rotation, om);
    }

    @Override
    public Rectangle GetBounds() {
        return rectangle;
    }

    //TODO: write the intersects
    @Override
    public boolean intersects(GameObject s) {
        return false;
    }


    //Read the image
    public void setImage(Image i)
    {
        image = i;
        width = i.getWidth();
        height = i.getHeight();
    }

    public void setImage(String filename) throws FileNotFoundException {
        FileInputStream inputted = new FileInputStream(filename);
        Image image = new Image(inputted);
        setImage(image);
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.drawImage( image, position.getX(), position.getY() );
    }

    //update the position
    @Override
    public void update(double time) {
        //position = position.add(velocity.multiply(time));
        //TODO: for this after bullet created, if bullet touch the brick, brick should be removed
    }
}
