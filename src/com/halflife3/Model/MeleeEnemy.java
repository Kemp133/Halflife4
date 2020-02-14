package com.halflife3.Model;

import com.halflife3.Controller.ObjectManager;

public class MeleeEnemy extends Enemy{

    public MeleeEnemy(Vector2 position, Vector2 velocity, short rotation, ObjectManager om) {
        super(position, velocity, rotation, om, 1);
    }

    //TODO: overlapping hitbox means damage, if not, move to player
    @Override
    public void attackPattern(){
        //look for closest player
        //if the player is far: idle mode, patrols or just stand still
        //if the player is close enough, moveTo player.
    }
}
