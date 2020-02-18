package com.halflife3.Networking.Packets;

import com.halflife3.Controller.ObjectManager;
import com.halflife3.Model.Player;
import com.halflife3.Model.Vector2;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

public class AllPlayersPacket implements Serializable {

    private static final long serialVersionUID = 7L;

    private static HashSet<Player> allPlayers = new HashSet<>();

    public AllPlayersPacket(Vector2[] startPositions) {
        Vector2 startVel = new Vector2(0, 0);
        ObjectManager om = new ObjectManager();

        for (Vector2 startPosition : startPositions) {
            Player bot = new Player(startPosition, startVel, (short) 0, om);
            bot.setAI(true);
            allPlayers.add(bot);
        }
    }

    public void addPlayer(Player toAdd) {
        for (Player player : allPlayers) {
            if (player.isAI()) {
                allPlayers.remove(player);
                allPlayers.add(toAdd);
            }
        }

    }

    public HashSet<Player> getAllPlayers() {
        return allPlayers;
    }

}
