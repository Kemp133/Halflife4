package com.halflife3.Networking.Client;

import com.halflife3.Model.Vector2;
import com.halflife3.Networking.Packets.ConnectPacket;
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
            System.out.println("Joined group: " + group);
        } catch (ConnectException e) {
            System.out.println("Unable to join the group");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getHostInfo() {
        try {
//            Receives the welcome (Test) packet
            byte[] firstBuf = new byte[5000];
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
//        TODO: If position has changed send it to the server
        while(running) {
            System.out.println("Client running");
            wait(10000);
        }

        running = false;
    }

    public Object receivePacket() {
        Object o = null;

        try {
//            Receives the packet
            byte[] recBuf = new byte[5000];
            DatagramPacket packet = new DatagramPacket(recBuf, recBuf.length);
            serverSocket.receive(packet);
//            Converts the packet byte array into an object
            ByteArrayInputStream byteStream = new ByteArrayInputStream(recBuf);
            ObjectInputStream instream = new ObjectInputStream(new BufferedInputStream(byteStream));
            o = instream.readObject();
            instream.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return o;
    }

    private void wait(int millis) {
        try {
            Thread.sleep(millis);
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
}
