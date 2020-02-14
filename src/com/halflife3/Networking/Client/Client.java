package com.halflife3.Networking.Client;

import com.halflife3.Model.Vector2;
import com.halflife3.Networking.Packets.ConnectPacket;
import com.halflife3.Networking.Packets.DisconnectPacket;
import com.halflife3.Networking.Packets.WelcomePacket;
import com.halflife3.Networking.Server.Server;

import java.io.*;
import java.net.*;

public class Client implements Runnable {

//    For "catching" the server
    protected MulticastSocket serverSocket = null;
    protected InetAddress group = null;

//    For sending packets to the server
    private InetAddress hostAddress;
    private DatagramSocket outSocket;

//    Client's position on the map
    private Vector2 position;

//    isRunning
    private boolean running = false;

    public void joinGroup() {
        try {
            serverSocket = new MulticastSocket(Server.MULTICAST_PORT);
            group = InetAddress.getByName(Server.MULTICAST_ADDRESS);
            serverSocket.joinGroup(group);
            System.out.println("Joined group: " + group.getHostName());
        } catch (ConnectException e) {
            System.out.println("Unable to join the group");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getHostInfo() {
        try {
//            Receives the welcome (Test) packet
            byte[] firstBuf = new byte[objectToByteArray(new WelcomePacket()).length];
            DatagramPacket firstPacket = new DatagramPacket(firstBuf, firstBuf.length);
            System.out.println("Looking for host...");
            serverSocket.receive(firstPacket);

//            Gets the server's address
            hostAddress = firstPacket.getAddress();

            System.out.println("Host found: " + hostAddress);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
//        Lets the server know we have connected
        ConnectPacket join = new ConnectPacket();
        byte[] tempBuf = objectToByteArray(join);

        DatagramPacket poke = new DatagramPacket(tempBuf, tempBuf.length, hostAddress, Server.LISTENER_PORT);
        try {
            outSocket = new DatagramSocket();
            outSocket.send(poke);
        } catch (IOException e) {
            e.printStackTrace();
        }

        running = true;
        new Thread(this).start();
    }

    @Override
    public void run() {
//        TODO: If position has changed send Vector2 to the server
        while(running) {
            System.out.println("Client running");
            for (int i = 10; i > 0; i--)
                waitASecond();
        }

        running = false;
        close();
    }

    public void close() {
        DisconnectPacket leave = new DisconnectPacket();
        byte[] tempBuf = objectToByteArray(leave);

//        TODO: Change Server.LISTENER_PORT to a client specific port
        DatagramPacket dc = new DatagramPacket(tempBuf, tempBuf.length, hostAddress, Server.LISTENER_PORT);
        try {
            outSocket.send(dc);
        } catch (IOException e) {
            e.printStackTrace();
        }

        serverSocket.close();
        outSocket.close();
    }

    public Object receivePacket() {
        Object o = null;

        try {
            byte[] recBuf = new byte[5000];
            DatagramPacket packet = new DatagramPacket(recBuf, recBuf.length);
            serverSocket.receive(packet);
            o = byteArrayToObject(recBuf);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return o;
    }

    private void waitASecond() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
}
