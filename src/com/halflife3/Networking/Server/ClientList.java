package com.halflife3.Networking.Server;

import com.halflife3.Networking.Packets.PositionPacket;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;

public class ClientList {

	public HashMap<InetAddress, ConnectedToServer> connectedList;
	public HashMap<String, PositionPacket>         positionList;
	public HashSet<String>                         connectedIPs;

	public ClientList() {
		connectedList = new HashMap<>();
		positionList  = new HashMap<>();
		connectedIPs  = new HashSet<>();
	}

}
