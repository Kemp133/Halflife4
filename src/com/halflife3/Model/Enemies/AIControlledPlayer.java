package com.halflife3.Model.Enemies;

import com.halflife3.Model.Enemy;
import com.halflife3.Model.Player;
import com.halflife3.Model.Vector2;

public class AIControlledPlayer extends Enemy {
    public AIControlledPlayer(Vector2 position, Vector2 velocity) {
        super(position, velocity, 1);
    }

    @Override
    public void attackPattern(Player[] playerList) {

    }
}
