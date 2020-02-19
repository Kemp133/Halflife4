/*
package com.halflife3.Model;

import com.halflife3.Controller.ObjectManager;

import java.io.FileNotFoundException;

//an enemy that can both walk to and shoot at Enemy
public class finalEnemy extends Enemy{
    public finalEnemy(Vector2 position, Vector2 velocity, short rotation, ObjectManager om, int life) {
        super(position, velocity, rotation, om, life);
        try {
            setImage("res/arrowEnemy.png");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void attackPattern(Player[] playerList) {
        while(health < 0){
            //move to player
            moveTo(closestPlayer(playerList));
            //change rotation
            //shoot at player
        }
        death();
    }
}
*/
