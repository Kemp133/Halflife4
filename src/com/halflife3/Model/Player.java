package com.halflife3.Model;

import com.halflife3.Networking.Packets.PositionPacket;
import com.halflife3.View.Camera;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.shape.Circle;
import javafx.scene.transform.Affine;

import java.io.FileInputStream;
import java.io.IOException;

public class Player extends Controllable {
    //region Variables
    public Circle circle;
    private Image image_w;
    private Vector2 spawn_point;
    private Vector2 original_position;
    private float  moveSpeed = 100;
    protected int  health;
    int mode = 0;
    boolean bulletShot = false;
    boolean is_moving = false;
    //endregion

    public Player(Vector2 position, Vector2 velocity) {
        super(position, velocity);
        keys.add("player");
        circle = new Circle(position.getX() + 18, position.getY() + 18, 17);
        affine = new Affine();
        packetToSend = new PositionPacket();
        packetToSend.spawnX = packetToSend.orgPosX = position.getX();
        packetToSend.spawnY = packetToSend.orgPosY = position.getY();
        packetToSend.velX = velocity.getX();
        packetToSend.velY = velocity.getY();
    }

    //region Overridden super methods
    @Override
    public Circle getBounds() {
        return circle;
    }

    public void setSprite2(String pathToSprite) {
        try (var fis = new FileInputStream(pathToSprite)) {
            image_w = new Image(fis);
        } catch (IOException e) {
            System.err.println("Image not found!");
        }

    }

    @Override
    public void render(GraphicsContext gc) {
        gc.save(); // Save default transform
        gc.setTransform(affine);
        if ((mode % 5) == 0 && is_moving) {
            gc.drawImage(image_w, position.getX() - Camera.GetOffset().getX(), position.getY() - Camera.GetOffset().getY());
            mode++;
        } else {
            gc.drawImage(sprite, position.getX() - Camera.GetOffset().getX(), position.getY() - Camera.GetOffset().getY());
            if (mode < 11)
                mode++;
            else
                mode = 0;
        }
        gc.restore(); // Restore default transform
    }

    @Override
    public void update(double time) {
        original_position = new Vector2(position);
        position = position.add(new Vector2(velocity).multiply(time));
        if (original_position.equals(position))
            is_moving = false;
        circle.setCenterX(position.getX() + 18);
        circle.setCenterY(position.getY() + 18);
    }
    //endregion

    public void collision(boolean if_collision, double time) {
        if (if_collision) {
            this.position = original_position.subtract(this.velocity.multiply(time));
            circle.setCenterX(position.getX() + 18);
            circle.setCenterY(position.getY() + 18);
        }

        velocity.reset();
    }

    public void addVelocity(Vector2 toAdd) {
        velocity = velocity.add(toAdd);
    }
    public void resetPosition() {
        original_position = spawn_point;
    }
}