package com.halflife3.Model;

import com.halflife3.Controller.ObjectManager;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Player extends GameObject {
    public double width = 49;
    public double height = 43;
    public Rectangle rectangle = new Rectangle(position.getX(), position.getY(), width, height);
    private Image image;
    private Vector2 original_position;

    private float moveSpeed = 100;

    //Initialize a player
    public Player(Vector2 position, Vector2 velocity, short rotation, ObjectManager om, int ID) {
        super(position, velocity, rotation, om,ID);
        keys.add("player");
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
    public void setImage(Image i) {
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
        gc.drawImage(image, position.getX() , position.getY() );
    }

    //update the position
    @Override
    public void update(double time) {
        original_position = new Vector2(position);
        position = position.add(new Vector2(velocity).multiply(time));
        rectangle.setX(position.getX());
        rectangle.setY(position.getY());
    }

    public void collision(boolean if_collision, double time) {
        if (if_collision) {
            this.position = original_position.subtract(this.velocity.multiply(2*time));
            rectangle.setX(this.position.getX());
            rectangle.setY(this.position.getY());
        }

        velocity.reset();
    }

    public void addVelocity(Vector2 toAdd) {
        velocity = velocity.add(toAdd);
    }

    public float getMoveSpeed() { return moveSpeed; }
    public void setMoveSpeed(float speed) { moveSpeed = speed; }
}
