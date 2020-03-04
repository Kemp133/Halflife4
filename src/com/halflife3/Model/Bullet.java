package com.halflife3.Model;

//import com.halflife3.GameUI.AudioForGame;
import com.halflife3.View.Camera;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Circle;

public class Bullet extends Sprite {
    private Circle circle = new Circle(position.getX()+4,position.getY()+4, 4);

    public Bullet(Vector2 position, Vector2 velocity, short rotation) {
        super(position, velocity, rotation);
        keys.add("Bullet");
        setSprite("res/bullet.png");
    }

    @Override
    public Circle getBounds() {
        return circle;
    }

    @Override
    public boolean intersects(GameObject s) {
        return false;
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.drawImage(sprite, getPosX() - Camera.GetOffset().getX(), getPosY() - Camera.GetOffset().getY());
    }

    @Override
    public void update(double time) {
        position = position.add(new Vector2(velocity).multiply(time));
        circle.setCenterX(position.getX()+4);
        circle.setCenterY(position.getY()+4);
    }
}
