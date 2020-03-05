//package com.halflife3.Model.powerUps;
//
//import com.halflife3.Controller.ObjectManager;
//import com.halflife3.Model.GameObject;
//import com.halflife3.Model.Vector2;
//import javafx.scene.canvas.GraphicsContext;
//import javafx.scene.image.Image;
//import javafx.scene.shape.Rectangle;
//import javafx.scene.shape.Shape;
//
//public class boost extends GameObject {
//    private Image sprite;
//    private Rectangle hitBox = new Rectangle(24,24);
//    public boost(Vector2 position, Vector2 velocity, double rotation, ObjectManager om) {
//        super(position, velocity, rotation, om);
//    }
//
//    @Override
//    public Shape GetBounds() {
//        return null;
//    }
//
//    @Override
//    public boolean intersects(GameObject s) {
//        return false;
//    }
//
//    @Override
//    public void render(GraphicsContext gc, Vector2 offset) {
//
//    }
//
//    @Override
//    public void update(double time) {
//
//    }
//}
