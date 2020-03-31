package com.halflife3.Mechanics.GameObjects;

import com.halflife3.Mechanics.Vector2;
import com.halflife3.Networking.Client.ClientGame;
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
    private Vector2 originalPosition;
    private PositionPacket packetToSend;
//    private int mode = 0;
    public float stand = 0;
    public boolean bulletShot = false;
    private boolean isMoving = false;
    private boolean holdsBall = false;
    //endregion

    public Player(Vector2 position, Vector2 velocity) {
        super(position, velocity);
        acceleration = new Vector2(0,0);
        keys.add("player");
        setSprite("res/Sprites/PlayerSkins/Cosmo_Hovering.png");
        setSprite2("res/Sprites/PlayerSkins/Cosmo_Moving.png");
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

    @Override
    public void render(GraphicsContext gc) {
        double posX = position.getX() - Camera.GetOffset().getX();
        double posY = position.getY() - Camera.GetOffset().getY();

        gc.save();
        gc.setTransform(affine);

        if (isMoving) {
            gc.drawImage(sprite2, posX, posY);
        } else gc.drawImage(sprite, posX, posY);

//        if (mode % 6 == 0 && isMoving) {
//            gc.drawImage(sprite2, posX, posY);
//            mode++;
//        } else {
//            gc.drawImage(sprite, posX, posY);
//            mode = (mode < 10) ? mode + 1 : 0;
//        }

        gc.restore();
    }

    @Override
    public void update(double time) {
        if (stand <= ClientGame.STUN_DURATION && stand != 0){
            velocity.setX(0);
            velocity.setY(0);
            acceleration.setX(0);
            acceleration.setY(0);
            stand--;
        } else {
            originalPosition = new Vector2(position);
            Vector2 previous_Vel = new Vector2(velocity);
            if(velocity.getX()*previous_Vel.subtract(acceleration).getX()>0)
                velocity.subtract(acceleration);
            position.add(new Vector2(velocity).multiply(time));
            isMoving = !originalPosition.equals(position);
            circle.setCenterX(position.getX() + getWidth() / 2 + 1);
            circle.setCenterY(position.getY() + getHeight() / 2 + 1);
            if(stand!=0)
                stand--;
        }
    }
    //endregion

    public void setMoving(boolean is_moving) {
        this.isMoving = is_moving;
    }

    public void setSprite2(String pathToSprite) {
        try (FileInputStream fis = new FileInputStream(pathToSprite)) {
            sprite2 = new Image(fis);
        } catch (IOException e) {
            System.err.println("Image not found!");
        }

    }

    public void collision(Bricks block, double time) {
        Vector2 dir = new Vector2(block.getPosX() - originalPosition.getX(),
                block.getPosY() - originalPosition.getY());

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
        originalPosition = spawn_point;
    }

    public void setBulletShot(boolean bulletShot) {
        this.bulletShot = bulletShot;
    }

    public boolean isHoldingBall() {
        return holdsBall;
    }

    public void setHoldsBall(boolean holdsBall) {
        this.holdsBall = holdsBall;
    }

    public PositionPacket getPacketToSend() {
        packetToSend.posX = getPosX();
        packetToSend.posY = getPosY();
        packetToSend.velX = getVelX();
        packetToSend.velY = getVelY();
        packetToSend.degrees = degrees;
        packetToSend.bulletShot = bulletShot;
        packetToSend.holdsBall = holdsBall;
        return packetToSend;
    }
}