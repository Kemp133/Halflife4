package com.halflife3.Model;

import com.halflife3.Controller.ObjectManager;

import java.io.FileNotFoundException;

public class MeleeEnemy extends Enemy{

    public MeleeEnemy(Vector2 position, Vector2 velocity, short rotation, ObjectManager om , int id) throws FileNotFoundException {
        super(position, velocity, rotation, om, 1 , id);
        setImage("res/pixil-frame-0.png");
    }

    //TODO: overlapping hitbox means damage, if not, move to player
    @Override
    //enemy does not care sight or distance, it will charge at player
    public void attackPattern(Player[] playerList){

        //TODO: the folloing code only chases the closest player on spawn, it will not change aggro
        while(health > 0){
            moveTo(closestPlayer(playerList));
        }
        death();
    }
}
