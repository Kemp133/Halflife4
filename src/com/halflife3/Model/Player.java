package com.halflife3.Model;

import com.halflife3.Controller.ObjectManager;
import com.halflife3.Networking.Packets.PositionPacket;
import com.halflife3.View.MapRender;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Affine;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Deque;

public class Player extends GameObject {
    public double width = 40;
    public double height = 35;
    public Rectangle rectangle;
    private Image image;
    private Vector2 spawn_point;
    private Vector2 original_position;
    private String ipOfClient;
    private boolean AI = true;
    private float moveSpeed = 100;
    private Affine rotate;
    private PositionPacket packetToSend;
    protected int health;

    public Player(Vector2 position, Vector2 velocity, double rotation, ObjectManager om) {
        super(position, velocity, rotation, om);
        keys.add("player");
        rectangle = new Rectangle(position.getX(), position.getY(), width, height);
        rotate = new Affine();
        packetToSend = new PositionPacket();
        packetToSend.spawnX = packetToSend.orgPosX = position.getX();
        packetToSend.spawnY = packetToSend.orgPosY = position.getY();
        packetToSend.velX = velocity.getX();
        packetToSend.velY = velocity.getY();
        packetToSend.rotation = rotation;
    }

    @Override
    public Rectangle GetBounds() {
        return rectangle;
    }

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

    public String getIpOfClient() {
        return ipOfClient;
    }

    public void setIpOfClient(String ipOfClient) {
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

    public Affine getRotate() {
        return rotate;
    }

    public PositionPacket getPacketToSend() {
        packetToSend.velY = velocity.getY();
        packetToSend.velX = velocity.getX();
        packetToSend.orgPosX = position.getX();
        packetToSend.orgPosY = position.getY();
        return packetToSend;
    }

    public void setPacketToSend(PositionPacket packetToSend) {
        this.packetToSend = packetToSend;
    }

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
            if(scanArea.intersects(wall.GetBounds().getBoundsInLocal())){
                return true;
            }
        }
        return false;
    }

    //TODO: need to move to specific location, also avoiding the obstacle

    //TODO: add a death animation
    public void death(){
        if (health == 0){
            selfDestroy();
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

}
