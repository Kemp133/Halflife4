package com.halflife3.Networking.Server;

import com.halflife3.Model.Vector2;
import com.halflife3.Networking.NetworkingUtilities;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class ConnectedToServer implements Runnable {
    private InetAddress clientAddress;
    private Vector2 position;

    private boolean running;
    private DatagramSocket uniqueSocket = null;
    private EventListenerServer listenerServer;

    public ConnectedToServer(InetAddress address, int clientListeningPort) {
        clientAddress = address;
        position = new Vector2(0, 0); // startX and startY
        listenerServer = new EventListenerServer();

        try {
            uniqueSocket = new DatagramSocket(clientListeningPort);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            System.out.println("Waiting for position change of: " + clientAddress); // For logging atm
            connectionListener();
        }
    }

    public void close() {
        running = false;
        uniqueSocket.close();
    }

    private void connectionListener() {
        byte[] posBuf = new byte[NetworkingUtilities.objectToByteArray(new Vector2()).length]; //Only Vector2 packets atm
        DatagramPacket incPos = new DatagramPacket(posBuf, posBuf.length);

        try {
            uniqueSocket.receive(incPos);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Object receivedPosition = NetworkingUtilities.byteArrayToObject(posBuf);
        listenerServer.received(receivedPosition, clientAddress);
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(Vector2 position) {
        this.position = position;
    }
}
