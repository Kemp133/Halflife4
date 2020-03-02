package com.halflife3.Networking.Server;

import com.halflife3.Model.Vector2;
import com.halflife3.Networking.Packets.PositionPacket;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class ConnectedToServer implements Runnable {
    private InetAddress clientAddress;
    private Vector2 client_position;
    private Vector2 spawnPoint;
    private boolean running;
    private DatagramSocket uniqueSocket;
    private EventListenerServer listenerServer;
    private int lengthOfPackets;

    public ConnectedToServer(InetAddress address, int clientListeningPort, Vector2 spawnPoint) {
        clientAddress = address;
        this.spawnPoint = client_position = spawnPoint;
        listenerServer = new EventListenerServer();
        lengthOfPackets = objectToByteArray(new PositionPacket()).length;

        try { uniqueSocket = new DatagramSocket(clientListeningPort); }
        catch (SocketException e) { e.printStackTrace(); }
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            connectionListener();
        }
    }

    public void close() {
        running = false;
        uniqueSocket.close();
    }

    private void connectionListener() {
        byte[] posBuf = new byte[lengthOfPackets];
        DatagramPacket incPos = new DatagramPacket(posBuf, posBuf.length);

        double x = System.nanoTime();
        try { uniqueSocket.receive(incPos); } catch (IOException e) {
            return;
        }
        System.out.println("Time took to receive packet: " + (System.nanoTime() - x));

        Object receivedPosition = byteArrayToObject(posBuf);
        listenerServer.received(receivedPosition, clientAddress);
    }

    private Object byteArrayToObject(byte[] buf) {
        Object o = null;

        try {
            ByteArrayInputStream byteStream = new ByteArrayInputStream(buf);
            ObjectInputStream instream = new ObjectInputStream(new BufferedInputStream(byteStream));
            o = instream.readObject();
            instream.close();
        } catch (EOFException e) {
            System.out.println("Byte array 'posBuf' in ConnectedToServer class too small");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return o;
    }

    private static byte[] objectToByteArray(Object o) {
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

    public Vector2 getPosition() {
        return client_position;
    }

    public void setPosition(Vector2 position) {
        this.client_position = position;
    }

    public Vector2 getSpawnPoint() {
        return spawnPoint;
    }
}
