//package com.halflife3.Model;
//
//import javafx.scene.canvas.GraphicsContext;
//import javafx.scene.image.Image;
//import javafx.scene.shape.Rectangle;
//
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//
//
//public abstract class Enemy extends GameObject {
//    protected double width;
//    protected double height;
//    private Image image;
//
//    //Initialize a player
//    public Enemy(Vector2 position, Vector2 velocity, short rotation){
//        super(position,velocity,rotation);
//    }
//
//    @Override
//    public Rectangle GetBounds() {
//        //Create rectangle object(one of node in javafx)
//        return new Rectangle(this.position.getX(), this.position.getY(), this.width, this.height);
//    }
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
//        this.position = this.position.add(this.velocity.multiply(time));
//    }
//
//    public void addVelocity(Vector2 toAdd) {
//        this.velocity.add(toAdd);
//    }
//}
