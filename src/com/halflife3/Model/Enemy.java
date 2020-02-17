package com.halflife3.Model;

import com.halflife3.Controller.ObjectManager;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public abstract class Enemy extends GameObject {
    protected double width;
    protected double height;
    protected Image image;
    protected int health;

    public void setHealth(int health) {
        this.health = health;
    }

    public int getHealth() {
        return health;
    }

    //Initialize a player
    public Enemy(Vector2 position, Vector2 velocity, short rotation, ObjectManager om, int life,int ID){
        super(position,velocity,rotation, om,ID);
        health = life;
    }

    @Override
    public Rectangle GetBounds() {
        //Create rectangle object(one of node in javafx)
        return new Rectangle(position.getX(), position.getY(), width, height);
    }
    //TODO: write the intersects, this checks if the hitbox of GameObject s overlaps with this enemy
    @Override
    public boolean intersects(GameObject s) {
        return false;
    }


    //Read the image, sets it as the image of this Enemy
    public void setImage(Image i)
    {
        image = i;
        width = i.getWidth();
        height = i.getHeight();

    }

    public void setImage(String filename) throws FileNotFoundException {
        FileInputStream inputted = new FileInputStream(filename);
        Image image = new Image(inputted);
        setImage(image);
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.drawImage( image, position.getX(), position.getY() );
    }

    //update the position
    @Override
    public void update(double time) {
        position = position.add(velocity.multiply(time));
    }

    //adds velocity
    public void addVelocity(Vector2 toAdd) {
        velocity = velocity.add(toAdd);
    }

    //TODO: need to move to specific location, also avoiding the obstacle
    public void moveTo(Vector2 position){
        //step 1: find shortest path without walls
        //step 2: move to target
        this.position = getPath(this.position, position);
    }
    public void moveTo(GameObject entity){
        //step 1: find shortest path without walls
        this.position = getPath(this.getPosition(), entity.getPosition());
        //step 2: move to target
    }
    //this method gets the path between two positions
    public Vector2 getPath(Vector2 original , Vector2 position){
        //create the list
        if(original == position){ return position;}
        //upEmpty?
        Vector2 up = new Vector2(original.getX(), original.getY() + 1);
        //rightEmpty?
        Vector2 right = new Vector2(original.getX() +1 , original.getY());
        //downEmpty?
        Vector2 down = new Vector2(original.getX(), original.getY() - 1);
        //leftEmpty?
        Vector2 left = new Vector2(original.getX() - 1, original.getY());
        double udis = up.squareDistance(position);
        double rdis = right.squareDistance(position);
        double ddis = down.squareDistance(position);
        double ldis = left.squareDistance(position);

        double[] shortest = {udis, rdis, ddis, ldis};

        if (hasWall(up)){ shortest[0] =  Math.pow(2, 10);}
        if (hasWall(right)){ shortest[1] = Math.pow(2, 10);}
        if (hasWall(down)){ shortest[2] = Math.pow(2, 10);}
        if (hasWall(left)){ shortest[3] = Math.pow(2, 10);}

        int closestRoute = FindSmallest(shortest);
        Vector2 chosen = null;
        switch(closestRoute){
            case 0:
                chosen =up;
                break;
            case 1:
                chosen = right;
                break;
            case 2:
                chosen = down;
                break;
            case 3:
                chosen = left;
                break;
        }
        return chosen;
    }

    //Auxillary method for getPath
    public static int FindSmallest (double [] arr1) {
        int index = 0;
        double min = arr1[index];

        for (int i=1; i<arr1.length; i++) {

            if (arr1[i] < min) {
                min = arr1[i];
                index = i;
            }
        }
        return index;
    }

    //TODO: create a method that finds walls
    public boolean hasWall(Vector2 location){
        return false;
    }

    //TODO: need to move to specific location, also avoiding the obstacle

    //this method removes the enemy from the screen
    //TODO: add a death animation
    public void death(){
       if (health == 0){
          selfDestroy();
       }
    }
    public abstract void attackPattern();

    //TODO: Find closest player
    /*
    public Player closestPlayer(){

    }*/
}
