package com.halflife3.Networking.Packets;

import com.halflife3.Controller.ObjectManager;
import com.halflife3.Model.Player;
import com.halflife3.Model.Vector2;

import java.io.Serializable;
import java.util.HashSet;

public class AllPlayersPacket implements Serializable {

    private static final long serialVersionUID = 7L;

    private Vector2 startVel = new Vector2(0, 0);
    private ObjectManager om = new ObjectManager();

    private static HashSet<Player> allPlayers = new HashSet<>();

    public AllPlayersPacket(Vector2[] startPositions) {
        for (Vector2 startPosition : startPositions) {
            Player bot = new Player(startPosition, startVel, (short) 0, om);
            bot.setSpawn_point(startPosition);
            bot.setAI(true);
            allPlayers.add(bot);
        }
    }

    public void addPlayer(Player toAdd) {
        for (Player player : allPlayers) {
            if (player.isAI()) {
                allPlayers.remove(player);
                toAdd.setPosition(player.getSpawn_point());
                toAdd.setSpawn_point(player.getSpawn_point());
                allPlayers.add(toAdd);
                break;
            }
        }
    }

    public void removePlayer(Player toRemove) {
        for (Player player : allPlayers) {
            if (player.getIpOfClient().getHostAddress().equals(toRemove.getIpOfClient().getHostAddress())) {
                allPlayers.remove(player);
                Player bot = new Player(toRemove.getSpawn_point(), startVel, (short) 0, om);
                bot.setSpawn_point(toRemove.getSpawn_point());
                bot.setAI(true);
                allPlayers.add(bot);
                break;
            }
        }
    }

    public HashSet<Player> getAllPlayers() {
        return allPlayers;
    }

}
