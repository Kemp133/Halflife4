package com.halflife3.Networking.Server;

import java.net.InetAddress;
import java.util.HashMap;

public class ClientPositionHandlerServer {

    public static HashMap<InetAddress, ConnectedToServer> clientList = new HashMap<>();
}
