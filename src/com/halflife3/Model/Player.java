package com.halflife3.Model;

import com.halflife3.Controller.ObjectManager;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Affine;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.LinkedList;

public class Player extends GameObject implements Serializable {
    private static final long serialVersionUID = 6L;
    public double width = 40;
    public double height = 35;
    public Rectangle rectangle;
    private Image image;
    private Vector2 spawn_point;
    private Vector2 original_position;
    private InetAddress ipOfClient;
    private boolean AI = true;
    private float moveSpeed = 100;
    private Affine rotate;

    public Player(Vector2 position, Vector2 velocity, short rotation, ObjectManager om) {
        super(position, velocity, rotation, om);
        keys.add("player");
        rectangle = new Rectangle(position.getX(), position.getY(), width, height);
    }

    @Override
    public Rectangle GetBounds() {
        return rectangle;
    }

    //TODO: write the intersects
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
        gc.save(); // Save default transform
        gc.setTransform(rotate);
        gc.drawImage(image, position.getX() , position.getY());
        gc.restore(); // Restore default transform
    }

    @Override
    public void update(double time) {
        original_position = new Vector2(position);
        position = position.add(new Vector2(velocity).multiply(time));
        rectangle.setX(position.getX());
        rectangle.setY(position.getY());
    }

    public void collision(boolean if_collision, double time) {
        if (if_collision) {
            this.position = original_position.subtract(this.velocity.multiply(2*time));
            rectangle.setX(this.position.getX());
            rectangle.setY(this.position.getY());
        }

        velocity.reset();
    }

    public void addVelocity(Vector2 toAdd) {
        velocity = velocity.add(toAdd);
    }

    public float getMoveSpeed() { return moveSpeed; }

    public void setMoveSpeed(float speed) { moveSpeed = speed; }

    public InetAddress getIpOfClient() {
        return ipOfClient;
    }

    public void setIpOfClient(InetAddress ipOfClient) {
        this.ipOfClient = ipOfClient;
    }

    public boolean isAI() {
        return AI;
    }

    public void setAI(boolean AI) {
        this.AI = AI;
    }

    public Vector2 getSpawn_point() {
        return spawn_point;
    }

    public void setSpawn_point(Vector2 spawn_point) {
        this.spawn_point = spawn_point;
    }

    public void setRotate(Affine rotate) {
        this.rotate = rotate;
    }


}

  class Enemy extends GameObject {
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
        Vector2 up = new Vector2(original.getX(), original.getY() + 20);
        //rightEmpty?
        Vector2 right = new Vector2(original.getX() +20 , original.getY());
        //downEmpty?
        Vector2 down = new Vector2(original.getX(), original.getY() - 20);
        //leftEmpty?
        Vector2 left = new Vector2(original.getX() - 20, original.getY());
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
    public void attackPattern(Player[] playerList){

    }


}
