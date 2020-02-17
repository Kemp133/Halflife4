package com.halflife3.Networking.Server;

import com.halflife3.Model.Vector2;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class ConnectedToServer implements Runnable {
    private InetAddress clientAddress;
    private Vector2 client_position;
    private Vector2 server_position;

    private boolean running;
    private DatagramSocket uniqueSocket = null;
    private EventListenerServer listenerServer;

    public ConnectedToServer(InetAddress address, int clientListeningPort) {
        clientAddress = address;
        client_position = new Vector2(0, 0); // startX and startY
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
        byte[] posBuf = new byte[objectToByteArray(new Vector2()).length]; //Only Vector2 packets atm
        DatagramPacket incPos = new DatagramPacket(posBuf, posBuf.length);

        try {
            uniqueSocket.receive(incPos);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Object receivedPosition = byteArrayToObject(posBuf);
        listenerServer.received(receivedPosition, clientAddress);
    }

    private byte[] objectToByteArray(Object o) {
        byte[] sendBuf = null;

        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream outstream = new ObjectOutputStream(new BufferedOutputStream(byteStream));
            outstream.flush();
            outstream.writeObject(o);
            outstream.flush();
            sendBuf = byteStream.toByteArray();
            outstream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sendBuf;
    }

    private Object byteArrayToObject(byte[] buf) {
        Object o = null;

        try {
            ByteArrayInputStream byteStream = new ByteArrayInputStream(buf);
            ObjectInputStream instream = new ObjectInputStream(new BufferedInputStream(byteStream));
            o = instream.readObject();
            instream.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return o;
    }

    public Vector2 getPosition() {
        return client_position;
    }

    public void setPosition(Vector2 position) {
        this.client_position = position;
    }
}
