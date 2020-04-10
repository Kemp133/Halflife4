package com.halflife3.Mechanics.PowerUps;

import com.halflife3.GameObjects.Sprite;
import com.halflife3.Mechanics.Vector2;
import com.halflife3.View.Camera;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Rectangle;


public class Speedup extends Sprite {
    public Rectangle rectangle;
    public Speedup(Vector2 position, Vector2 velocity, String image) {
        super(position, velocity,image);
        keys.add("speed");
        rectangle = new Rectangle(position.getX(),position.getY(), getWidth(), getHeight());
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.drawImage(sprite, position.getX() - Camera.GetOffset().getX(),
                position.getY() - Camera.GetOffset().getY());
    }

    @Override
    public Rectangle getBounds() {
        return rectangle;
    }

    @Override
    public void update(double time) {

    }
}
