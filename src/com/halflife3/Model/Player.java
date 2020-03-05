package com.halflife3.Model;

import com.halflife3.Networking.Packets.PositionPacket;
import com.halflife3.View.Camera;
import com.halflife3.View.MapRender;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Affine;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Deque;

public class Player extends Sprite {
    //region Variables
//    public double width = 40;
//    public double height = 35;
//    public Rectangle rectangle;
    public  Circle  circle;
//    private Image image;
    private Image   image_w;
    private Vector2 spawn_point;
    private Vector2 original_position;
    private String ipOfClient;
    private boolean AI = true;
    private float moveSpeed = 100;
    private Affine rotate;
    private double degrees;
    private PositionPacket packetToSend;
    protected int health;
    int mode = 0;
    boolean is_moving = false;
    //endregion

    public Player(Vector2 position, Vector2 velocity, double rotation) {
        super(position, velocity, rotation);
        keys.add("player");
        //rectangle = new Rectangle(position.getX(), position.getY(), width, height);
        circle = new Circle(position.getX()+18,position.getY()+18,17);
        rotate = new Affine();
        packetToSend = new PositionPacket();
        packetToSend.spawnX = packetToSend.orgPosX = position.getX();
        packetToSend.spawnY = packetToSend.orgPosY = position.getY();
        packetToSend.velX = velocity.getX();
        packetToSend.velY = velocity.getY();
        packetToSend.degrees = this.degrees;

        try(FileInputStream fis = new FileInputStream("res/Player_walking.png")) {
            image_w = new Image(fis);
        } catch (IOException e) {
            System.err.println("Image not found!");
        }

    }

    //region Overridden super methods
    @Override
    public Circle getBounds() {
        return circle;
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.save(); // Save default transform
        gc.setTransform(rotate);
        if((mode % 5) == 0 && is_moving){
            gc.drawImage(image_w, position.getX() - Camera.GetOffset().getX() , position.getY()-Camera.GetOffset().getY());
            mode++;
        }
        else {
        gc.drawImage(sprite, position.getX() - Camera.GetOffset().getX() , position.getY() - Camera.GetOffset().getY());
            if(mode < 11)
                mode++;
            else
                mode = 0;
        }
        gc.restore(); // Restore default transform
    }

    @Override
    public void update(double time) {
        original_position = new Vector2(position);
        position = position.add(new Vector2(velocity).multiply(time));
        if(original_position.getX() == position.getX() && original_position.getY() == position.getY())
            is_moving = false;
        circle.setCenterX(position.getX()+18);
        circle.setCenterY(position.getY()+18);
    }
    //endregion

    public void collision(boolean if_collision, double time) {
        if (if_collision) {
            this.position = original_position.subtract(this.velocity.multiply(time));
            circle.setCenterX(position.getX()+18);
            circle.setCenterY(position.getY()+18);
        }

        velocity.reset();
    }

    public void addVelocity(Vector2 toAdd) {
        velocity = velocity.add(toAdd);
    }
    public void resetPosition() {
        original_position = spawn_point;
    }

    //region Degrees getter and setter
    public double getDegrees() {
        return degrees;
    }

    public void setDegrees(double degrees) {
        this.degrees = degrees;
    }
    //endregion

    //region MoveSpeed getter and setter
    public float getMoveSpeed() { return moveSpeed; }
    public void setMoveSpeed(float speed) { moveSpeed = speed; }
    //endregion

    //region IP getter and setter
    public String getIpOfClient() {
        return ipOfClient;
    }
    public void setIpOfClient(String ipOfClient) {
        this.ipOfClient = ipOfClient;
    }
    //endregion

    //region AI getter and setter
    public boolean isAI() {
        return AI;
    }
    public void setAI(boolean AI) {
        this.AI = AI;
    }
    //endregion

    //region SpawnPoint getter and setter
    public Vector2 getSpawn_point() {
        return spawn_point;
    }
    public void setSpawn_point(Vector2 spawn_point) {
        this.spawn_point = spawn_point;
    }
    //endregion

    //region Rotation getter and setter
    public void setRotate(Affine rotate) {
        this.rotate = rotate;
    }
    public Affine getRotate() {
        return rotate;
    }
    //endregion

    //region Packet getter and setter
    public PositionPacket getPacketToSend() {
        packetToSend.velY = getVelY();
        packetToSend.velX = getVelX();
        packetToSend.orgPosX = getPosX();
        packetToSend.orgPosY = getPosY();
        packetToSend.degrees = getDegrees();
        return packetToSend;
    }
    public void setPacketToSend(PositionPacket packetToSend) {
        this.packetToSend = packetToSend;
    }
    //endregion

    //region AI methods
    public void moveTo(Vector2 position){
        //step 1: find shortest path without walls
        //step 2: move to target
        this.position = getNextMove(this.position, position);
    }
    public void moveTo(GameObject entity){
        //step 1: find shortest path without walls
        //step 2: move to target
        this.position = getNextMove(this.getPosition(), entity.getPosition());
    }
    //this method gets the path between two positions
    public Vector2 getNextMove (Vector2 original , Vector2 position) {

        if (original == position) { return position; }

        Vector2 up = new Vector2(original.getX(), original.getY() + 20);
        Vector2 right = new Vector2(original.getX() + 20 , original.getY());
        Vector2 down = new Vector2(original.getX(), original.getY() - 20);
        Vector2 left = new Vector2(original.getX() - 20, original.getY());

        double udis = up.squareDistance(position);
        double rdis = right.squareDistance(position);
        double ddis = down.squareDistance(position);
        double ldis = left.squareDistance(position);

        double[] shortest = {udis, rdis, ddis, ldis};
//this avoids hitting the wall or moving to itself
        if (isWall(up, MapRender.get_list()) || udis == 0)    shortest[0] += 1000000;
        if (isWall(right, MapRender.get_list()) || rdis == 0) shortest[1] += 1000000;
        if (isWall(down, MapRender.get_list()) || ddis == 0)  shortest[2] += 1000000;
        if (isWall(left, MapRender.get_list()) || ldis == 0)  shortest[3] += 1000000;

        int closestRoute = FindSmallest(shortest);

        Vector2 chosen = null;
        switch (closestRoute) {
            case 0:
                chosen = up;
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

    public int FindSmallest(double[] arr1) {
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

    public Vector2 closestPlayerPosition(Player[] playerList) {

        double[] distance = {getDistance(playerList[0]), getDistance(playerList[1]), getDistance(playerList[2]), getDistance(playerList[3])};

        int closet = FindSmallest(distance);

        return playerList[closet].getPosition();
    }

    //TODO: create a method that checks for walls
    public boolean isWall(Vector2 location, Deque<Bricks> listOfWalls){
        Rectangle scanArea = new Rectangle(location.getX() - 10, location.getY() -10 , 20 , 20);
        for(Bricks wall: listOfWalls){
            if(scanArea.intersects(wall.getBounds().getBoundsInLocal())){
                return true;
            }
        }
        return false;
    }

    //TODO: need to move to specific location, also avoiding the obstacle

    //TODO: add a death animation
    public void death(){
        if (health == 0){
            destroy();
        }
    }

    //TODO: overlapping hitbox means damage, if not, move to player
    public void attackPattern(Player[] playerList){
        moveTo(closestPlayerPosition(playerList));
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getHealth() {
        return health;
    }

    public void setIs_moving(boolean is_moving) {
        this.is_moving = is_moving;
    }

    //endregion
}