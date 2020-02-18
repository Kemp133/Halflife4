package com.halflife3.Model;

import com.halflife3.Controller.ObjectManager;
import com.halflife3.Model.Interfaces.IRenderable;
import com.halflife3.Model.Interfaces.IUpdateable;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

import java.util.HashSet;

public abstract class GameObject implements IRenderable, IUpdateable {
    protected Vector2 position;
    protected Vector2 velocity;
    protected short rotation;
    protected ObjectManager objectManager;
    /** Using a HashSet to store a list of keys (e.g. indicate what type the GameObject is). HashSet for speed, and
     * to disallow duplicate keys from being added*/
    protected HashSet<String> keys;

    public GameObject(Vector2 position, Vector2 velocity, short rotation, ObjectManager om) {
        this.position = position;
        this.velocity = velocity;
        this.rotation = rotation;
        this.objectManager = om;
        om.addObject(this);
    }

    //An initialiser block
    {
        position = new Vector2();
        velocity = new Vector2();
        keys = new HashSet<>();
    }

    public boolean containsKey(String key) {
        return keys.contains(key);
    }

    /** Potentially going to remove this in favour of an interface at a later point. Using shape so that the shape can
     * change depending on what its acting as a collider for (e.g. a box or a circle) */
    public abstract Shape GetBounds();

    //region Position getters and Setters
    public double getX() { return position.getX(); }

    public double getY() { return position.getY(); }

    public Vector2 getPosition() { return new Vector2(position); }

    public void setPosition(Vector2 position) {
        this.position = position;
    }

    public void setPosition(double x, double y) {
        position.setX(x);
        position.setY(y);
    }
    //endregion

    //region Velocity Getters and Setters
    public double getVelX() { return velocity.getX(); }

    public double getVelY() { return velocity.getY(); }

    public Vector2 getVelocity() { return velocity; }

    public void setVelocity(Vector2 velocity) {
        this.velocity = velocity;
    }

    public void resetVelocity() {
        velocity.reset();
    }
    //endregion

    //region Rotation Getter and Setter
    public short getRotation() { return rotation; }

    public void setRotation(short toAdd) {
        rotation = (short)(toAdd % 360);
    }
    //endregion

    public abstract boolean intersects(GameObject s);

    //Gives the distance between an entity and this game object
    public double getDistance(GameObject entity){
        return position.distance(entity.getPosition());
    }

    public double squareDistance(GameObject entity){
        return position.squareDistance(entity.getPosition());
    }

    //Method to destroy this game object
    public void selfDestroy(){
        objectManager.removeObject(this);
    }

    public HashSet<String> getKeys() {
        return keys;
    }

    public void remove(){
        objectManager.removeObject(this);
    }
}
