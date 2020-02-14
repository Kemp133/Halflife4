package com.halflife3.Model;

import com.halflife3.Controller.ObjectManager;

public class explodeEnemy extends Enemy{

    public explodeEnemy(Vector2 position, Vector2 velocity, short rotation, ObjectManager om, int life) {
        super(position, velocity, rotation, om, life);
    }

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
