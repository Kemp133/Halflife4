package com.halflife3.Networking.Server;

import com.halflife3.Model.Player;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;

public class ClientPositionHandlerServer {

    public static HashMap<InetAddress, ConnectedToServer> clientList = new HashMap<>();
    public static HashSet<InetAddress> connectedIPs = new HashSet<>();
    public static HashSet<Player> playerList = new HashSet<>();

}
