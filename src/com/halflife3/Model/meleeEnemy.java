package com.halflife3.Model;

import com.halflife3.Controller.ObjectManager;
import javafx.scene.image.Image;

public class meleeEnemy extends Enemy{
    /*private Image image = "file:res/pixil-frame-0.png";
    public meleeEnemy(Vector2 position, Vector2 velocity, short rotation, int life){
        super(position,velocity,rotation, life);
    }*/

    public meleeEnemy(Vector2 position, Vector2 velocity, short rotation, ObjectManager om, int life) {
        super(position, velocity, rotation, om, life);
    }

    //TODO: overlapping hitbox means damage, if not, move to player
    public void attack(){

    }
}
