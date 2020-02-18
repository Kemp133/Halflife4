package com.halflife3.Model;

import com.halflife3.Controller.ObjectManager;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;

import java.beans.VetoableChangeListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;

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
        return new Rectangle(position.getX(), position.getY(), width, height);
    }
    //TODO: write the intersects, this checks if the hitbox of GameObject s overlaps with this enemy
    @Override
    public boolean intersects(GameObject s) {
        return false;
    }

    public void setImage(String file) throws FileNotFoundException {
        FileInputStream pngFile = new FileInputStream(file);
        image = new Image(pngFile);
        width = image.getWidth();
        height = image.getHeight();
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

   public void moveTo(Vector2 position){
        //step 1: find shortest path without walls
        //step 2: move to target
        this.position = getPath(this.position, position);
    }
    public void moveTo(GameObject entity){
        //step 1: find shortest path without walls
        //step 2: move to target
        this.position = getPath(this.getPosition(), entity.getPosition());
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

    /*The find path method
    The method would receive a destiny_pos as input
    return only one vector at every input
    requires to be called with the movement with enemy
    Algorithm:
    find neighbour of(original_pos);
    for(v : neighbour)
        if(getF(v) < getF(minF)
        v = minF
    return v;
    */
/*
    public Vector2 getPath(Vector2 original_pos, Vector2 player_pos){
        Vector2 res = original_pos;
        ArrayList<Vector2> neighbour = getNeighbour(original_pos);
        for(Vector2 v : neighbour){
            if(getF(res,player_pos) > getF(v,player_pos))
                res = v;
        }
        return res;
    }

    //get the MahattnDistance from v1 to v2
    private double getF(Vector2 v1, Vector2 v2) {
        return Math.abs(v1.getX()/40-v2.getX()/40)+Math.abs(v1.getY()/40-v2.getY()/40);
    }

    */
/*I don't know how to get the bricks_list so there is error *//*

    public ArrayList<Vector2> getNeighbour(Vector2 original_pos){
        ArrayList<Vector2> openList = new ArrayList<Vector2>();
        for(Vector2 v :Bricks_List) {
            //Problem the Player might be standing between two blocks
            for (double x = original_pos.getX() - 40; x <= original_pos.getX() + 40; x += 10) {
                for (double y = original_pos.getY() - 40; y <= original_pos.getY() + 40; y += 10) {
                    if (v.getX() == x && v.getY() == y && v != original_pos && !isWall(v)) {
                        openList.add(v);
                    }
                }
            }
        }
        return openList;
    }

    public boolean isWall(Vector2 v){
        */
/*Can I use color judging for wall judge?*//*

    }
*/


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

    //Find closest player
    public Player closestPlayer(Player[] playerList){
        LinkedList<Double> playerDistance = null;
        for (Player player : playerList) {
            playerDistance.add(getDistance(player));
        }

        Object[] playerDistanceArr = playerDistance.toArray();
        double[] playerDistanceArrayDouble = new double[0];

        for(int j = 0; j < playerDistanceArr.length; j++){
            playerDistanceArrayDouble[j] = (double) playerDistanceArr[j];
        }
        int closet = FindSmallest(playerDistanceArrayDouble);
        return playerList[closet];
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

    //TODO: overlapping hitbox means damage, if not, move to player
    public abstract void attackPattern(Player[] playerList);


}
