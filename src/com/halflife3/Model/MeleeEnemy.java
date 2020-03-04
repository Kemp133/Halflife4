package com.halflife3.Model;

import com.halflife3.Controller.ObjectManager;

public class MeleeEnemy extends Enemy{

    public MeleeEnemy(Vector2 position, Vector2 velocity, short rotation, ObjectManager om){
        super(position, velocity, rotation, 1);
    }

    //TODO: overlapping hitbox means damage, if not, move to player
    @Override
    public void attackPattern(Player[] playerList) {
        while (getHealth() > 0){
            moveTo(closestPlayerPosition(playerList));
        }
        death();
    }
}
