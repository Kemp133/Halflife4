package com.halflife3.Model;

import com.halflife3.Controller.ObjectManager;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class Bullet extends GameObject {
    Circle model;
    int radius = 10;

    double lifeTime = 5.0;

    public Bullet(Vector2 position, Vector2 velocity, short rotation, ObjectManager om) {
        super(position, velocity, rotation, om);
        model = new Circle(this.position.getX(), this.position.getY(), radius);
    }


    @Override
    public Rectangle GetBounds() {
        return new Rectangle(this.position.getX(), this.position.getY(), model.getRadius() * 2, model.getRadius() * 2);
    }

    @Override
    public boolean intersects(GameObject s) {
        return false;
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.fillOval(this.position.getX(), this.position.getY(), model.getRadius() * 2, model.getRadius() * 2);
    }

    @Override
    public void update(double time) {
        this.position = position.add(this.position.multiply(time));
    }
}
