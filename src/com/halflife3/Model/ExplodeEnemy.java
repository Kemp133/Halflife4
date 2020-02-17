package com.halflife3.Model;

import com.halflife3.Controller.ObjectManager;

import java.io.FileNotFoundException;


public class ExplodeEnemy extends Enemy{

    public ExplodeEnemy(Vector2 position, Vector2 velocity, short rotation, ObjectManager om , int id) throws FileNotFoundException {
        super(position, velocity, rotation, om, 2 ,id);
        setImage("res/ExplodeEnemy.png");
    }
    //TODO: write the method
    @Override
    public void attackPattern(Player[] playerList) {
        while(getDistance(closestPlayer(playerList)) > 10){
            moveTo(closestPlayer(playerList));
        }
        //TODO: deal damage in a 10 radius area
        death();
    }
}