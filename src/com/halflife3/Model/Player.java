package com.halflife3.Model;

import com.halflife3.Controller.ObjectManager;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.net.InetAddress;

public class Player extends GameObject implements Serializable {
    private static final long serialVersionUID = 6L;
    public double width = 40;
    public double height = 35;
    public Rectangle rectangle;
    private Image image;
    private Vector2 original_position;
    private InetAddress ipOfClient;
    private boolean AI = true;
    private float moveSpeed = 100;

    public Player(Vector2 position, Vector2 velocity, short rotation, ObjectManager om) {
        super(position, velocity, rotation, om);
        keys.add("player");
        rectangle = new Rectangle(position.getX()-width/2, position.getY()-height/2, width, height);
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

    public void setImage(String file) throws FileNotFoundException {
        FileInputStream pngFile = new FileInputStream(file);
        image = new Image(pngFile);
        width = image.getWidth();
        height = image.getHeight();
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.drawImage(image, position.getX() , position.getY() );
    }

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

    public InetAddress getIpOfClient() {
        return ipOfClient;
    }

    public void setIpOfClient(InetAddress ipOfClient) {
        this.ipOfClient = ipOfClient;
    }

    public boolean isAI() {
        return AI;
    }

    public void setAI(boolean AI) {
        this.AI = AI;
    }
}
