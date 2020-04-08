package com.halflife3.Networking.Server;

import com.halflife3.Networking.Packets.PositionPacket;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;

public class ClientListServer {

	public static HashMap<InetAddress, ConnectedToServer> clientList   = new HashMap<>();
	public static HashMap<String, PositionPacket>         positionList = new HashMap<>();
	public static HashSet<String>                         connectedIPs = new HashSet<>();

	public static void reset() {
        clientList   = new HashMap<>();
        positionList = new HashMap<>();
        connectedIPs = new HashSet<>();
    }

}
