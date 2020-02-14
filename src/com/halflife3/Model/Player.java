//package com.halflife3.Model;
//
//import com.halflife3.Controller.ObjectManager;
//import javafx.scene.canvas.GraphicsContext;
//import javafx.scene.image.Image;
//import javafx.scene.shape.Rectangle;
//
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.util.Deque;
//import java.util.Iterator;
//import java.util.Set;
//
//
//public class Player extends GameObject {
//    public double width = 49;
//    public double height = 43;
//    public Rectangle rectangle = new Rectangle(this.position.getX(), this.position.getY(), this.width, this.height);
//    private Image image;
//
//    private float moveSpeed = 150;
//
//    //Initialize a player
//    public Player(Vector2 position, Vector2 velocity, short rotation, ObjectManager om){
//        super(position,velocity,rotation, om);
//    }
//
//    @Override
//    public Rectangle GetBounds() {
//        //Create rectangle object(one of node in javafx)
//        //return new Rectangle(this.position.getX(), this.position.getY(), this.width, this.height);
//        return this.rectangle;
//    }
//
//    //TODO: write the intersects
//    @Override
//    public boolean intersects(GameObject s) {
//        return false;
//    }
//
//
//    //Read the image
//    public void setImage(Image i)
//    {
//        image = i;
//        width = i.getWidth();
//        height = i.getHeight();
//
//    }
//
//    public void setImage(String filename) throws FileNotFoundException {
//        FileInputStream inputted = new FileInputStream(filename);
//        Image image = new Image(inputted);
//        setImage(image);
//    }
//
//    @Override
//    public void render(GraphicsContext gc) {
//        gc.drawImage( image, position.getX(), position.getY() );
//    }
//
//    //update the position
//    @Override
//    public void update(double time) {
//
//    }
//
//    public void collision(Deque<Bricks> Blocks, double time) {
//
//        Vector2 orginal_position = new Vector2(this.position.getX(), this.position.getY());
//
//        this.position = this.position.add(this.velocity.multiply(time));
//
//        rectangle.setX(this.position.getX());
//        rectangle.setY(this.position.getY());
//
//        for (Bricks block : Blocks) {
//            if (block.GetBounds().intersects(this.rectangle.getBoundsInLocal())) {
//
//                this.position = orginal_position.subtract(this.velocity.multiply(time*20));
//                rectangle.setX(this.position.getX());
//                rectangle.setY(this.position.getY());
//                //this.velocity = new Vector2(0,0);
//            }
//        }
//        this.velocity.reset();
//    }
//    public void addVelocity(Vector2 toAdd) {
//        this.velocity.add(toAdd);
//    }
//
//    public float getMoveSpeed() { return this.moveSpeed; }
//    public void setMoveSpeed(float speed) { this.moveSpeed = speed; }
//}
