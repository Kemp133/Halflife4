package com.halflife3.Model;

import com.halflife3.Controller.ObjectManager;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;

import java.util.*;

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
    public Enemy(Vector2 position, Vector2 velocity, short rotation, ObjectManager om, int life){
        super(position,velocity,rotation, om);
        health = life;
    }

    @Override
    public Rectangle GetBounds() {
        //Create rectangle object(one of node in javafx)
        return new Rectangle(this.position.getX(), this.position.getY(), this.width, this.height);
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
        this.position = this.position.add(this.velocity.multiply(time));
    }

    //adds velocity
    public void addVelocity(Vector2 toAdd) {
        this.velocity.add(toAdd);
    }

    //TODO: need to move to specific location, also avoiding the obstacle
    public void moveTo(Vector2 position){
        //step 1: find shortest path without walls
        LinkedList<Vector2> path = getPath(this.position, position);
        //step 2: move to target
        while(!path.isEmpty()){
            this.position = path.pop();
            //wait(1);
        }
    }
    //this method gets the path between two positions
    public LinkedList<Vector2> getPath(Vector2 original , Vector2 position){
        //create the list
        LinkedList<Vector2> pathList = new LinkedList<Vector2>();
        if(original == position){ return pathList;}
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
        pathList = getPath(chosen , position);
        pathList.addFirst(chosen);
        return pathList;
    }
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
    public void moveTo(GameObject entity){
        //step 1: find shortest path without walls
        LinkedList<Vector2> path = getPath(this.getPosition(), entity.getPosition());
        //step 2: move to target
        this.position = path.pop();
    }
    //this method removes the enemy from the screen
    //TODO: add a death animation
    public void death(){
       if (health == 0){
          selfDestroy();
       }
    }
    public abstract void attackPattern();
}
