package com.halflife3.Model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;


public class Player extends GameObject{
    public double width = 49;
    public double height = 43;
    private Image image;

    //Initialize a player
    public Player(Vector2 position, Vector2 velocity, short rotation){
        super(position,velocity,rotation);
    }

    @Override
    public Rectangle GetBounds() {
        //Create rectangle object(one of node in javafx)
        Rectangle rectangle = new Rectangle(this.position.getX(), this.position.getY(), this.width, this.height);
        return rectangle;
    }

    //TODO: write the intersects
    @Override
    public boolean intersects(GameObject s) {
        return false;
    }


    //Read the image
    public void setImage(Image i)
    {
        image = i;
        width = i.getWidth();
        height = i.getHeight();
    }

    public void setImage(String filename)
    {
        Image i = new Image(filename);
        setImage(i);
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.drawImage( image, position.getX(), position.getY() );
    }

    //update the position
    @Override
    public void update(double time) {
        this.position = this.position.add(this.velocity.multiply(time));
    }

}
