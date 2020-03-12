package com.halflife3.Model.Enemies;

import com.halflife3.Model.Vector2;

public class TestAIPathFinding {
    public static void main(String[] args) {
        Vector2 position0 = new Vector2(0,0);
        Vector2 position1 = new Vector2(100,100);

        AIControlledPlayer bots0 = new AIControlledPlayer(position0, new Vector2(0,0));
        while(bots0.getPosition().distance(position1) > 25){
            bots0.moveTo(position1);
            System.out.println(bots0.getPosition());
        }
    }
}
