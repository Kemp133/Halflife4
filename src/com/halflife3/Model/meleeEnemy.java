package com.halflife3.Model;

public class meleeEnemy extends Enemy{
    public meleeEnemy(Vector2 position, Vector2 velocity, short rotation, int life){
        super(position,velocity,rotation, life);
    }

    //TODO: overlapping hitbox means damage, if not, move to player
    public void attack(){

    }
}
