package com.halflife3.Model;

import com.halflife3.Controller.ObjectManager;

public class ExplodeEnemy extends Enemy{

    public ExplodeEnemy(Vector2 position, Vector2 velocity, short rotation, ObjectManager om,int ID) {
        super(position, velocity, rotation, om, 2,ID);
    }
    //TODO: write the method
    @Override
    public void attackPattern() {
        //find closest player
        //move to player, if distance <= n
        //stop for 2 seconds
        //deal damage
        //delete itself
        death();
    }
}
