package com.halflife3.Mechanics.GameObjects;

import com.halflife3.Mechanics.Vector2;
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
    private Image sprite2;
    private Vector2 original_position;
    protected int health;
    int mode = 0;
    boolean bulletShot = false;
    boolean is_moving = false;
    //endregion

    public Player(Vector2 position, Vector2 velocity) {
        super(position, velocity);
        keys.add("player");
        setSprite("res/Player_pic.png");
        setSprite2("res/Player_walking.png");
        circle = new Circle(position.getX() + getWidth() / 2,
                position.getY() + getHeight() / 2,
                Math.max(getWidth(), getHeight()) / 2 + 1);
        affine = new Affine();
        packetToSend = new PositionPacket();
    }

    //region Overridden super methods
    @Override
    public Circle getBounds() {
        return circle;
    }

    public void setSprite2(String pathToSprite) {
        try (var fis = new FileInputStream(pathToSprite)) {
            sprite2 = new Image(fis);
        } catch (IOException e) {
            System.err.println("Image not found!");
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        double posX = position.getX() - Camera.GetOffset().getX();
        double posY = position.getY() - Camera.GetOffset().getY();

        gc.save();
        gc.setTransform(affine);

        if (mode % 6 == 0 && is_moving) {
            gc.drawImage(sprite2, posX, posY);
            mode++;
        } else {
            gc.drawImage(sprite, posX, posY);
            mode = (mode < 10) ? mode + 1 : 0;
        }

        gc.restore();
    }

    @Override
    public void update(double time) {
        original_position = new Vector2(position);
        position.add(new Vector2(velocity).multiply(time));
        is_moving = !original_position.equals(position);
        circle.setCenterX(position.getX() + getWidth() / 2 + 1);
        circle.setCenterY(position.getY() + getHeight() / 2 + 1);
    }
    //endregion

    public void collision(Bricks block, double time) {

        Vector2 dir = new Vector2(block.getPosX() - original_position.getX(),
                block.getPosY() - original_position.getY());

        if (Math.abs(dir.getX()) < 30 && Math.abs(dir.getY()) < 30) {
            do {
                position.add(new Vector2(velocity).multiply(-time));
                circle.setCenterX(position.getX() + getWidth() / 2 + 1);
                circle.setCenterY(position.getY() + getHeight() / 2 + 1);
            } while (block.getBounds().intersects(circle.getBoundsInLocal()));
            return;
        }

        if (dir.getX() < -30 && dir.getY() > -30 && dir.getY() < 30 && velocity.getX() < 0) velocity.setX(0);
        else if (dir.getX() > 30 && dir.getY() > -30 && dir.getY() < 30 && velocity.getX() > 0) velocity.setX(0);

        if (dir.getY() > 30 && dir.getX() > -30 && dir.getX() < 30 && velocity.getY() > 0) velocity.setY(0);
        else if (dir.getY() < -30 && dir.getX() > -30 && dir.getX() < 30 && velocity.getY() < 0) velocity.setY(0);
    }

    public void resetPosition() {
        original_position = spawn_point;
    }

    public void setBulletShot(boolean bulletShot) {
        this.bulletShot = bulletShot;
    }

    public PositionPacket getPacketToSend() {
        packetToSend.velY = getVelY();
        packetToSend.velX = getVelX();
        packetToSend.orgPosX = getPosX();
        packetToSend.orgPosY = getPosY();
        packetToSend.degrees = rotation;
        packetToSend.bulletShot = bulletShot;
        return packetToSend;
    }
}