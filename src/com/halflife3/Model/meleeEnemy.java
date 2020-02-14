package com.halflife3.Model;

import com.halflife3.Controller.ObjectManager;

public class meleeEnemy extends Enemy{

    public meleeEnemy(Vector2 position, Vector2 velocity, short rotation, ObjectManager om, int life) {
        super(position, velocity, rotation, om, life);
    }

    //TODO: overlapping hitbox means damage, if not, move to player
    @Override
    public void attackPattern(){
        //look for closest player
        //if the player is far: idle mode, patrols or just stand still
        //if the player is close enough, moveTo player.
    }
}
