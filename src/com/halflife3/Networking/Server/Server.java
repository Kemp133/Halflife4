package com.halflife3.Networking.Server;

import com.halflife3.Networking.Packets.ConnectPacket;
import com.halflife3.Networking.Packets.TestPacket;

import java.io.*;
import java.net.*;

public class Server implements Runnable {

    public static final int     MULTICAST_PORT      = 5555;
    public static final String  MULTICAST_ADDRESS   = "239.255.42.99";
    public static final int     LISTENER_PORT       = 5544;

    private boolean running = false;
    private DatagramSocket clientSocket;
    private EventListenerServer listenerServer;

    public void start() {
        try {
            clientSocket = new DatagramSocket(LISTENER_PORT);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        listenerServer = new EventListenerServer();
        new Thread(this).start();
    }

    @Override
    public void run() {
        running = true;
        System.out.println("Multicasting on port: " + MULTICAST_PORT);

//        Constantly multicasts TestPacket,
//        shuts server down if nobody connects for more than 10 seconds
        new Thread(() -> {
            int timeOut = 20;
            while (running && timeOut > 0) {
                sendWelcome();
                wait(500);

                if (ClientPositionHandler.positions.isEmpty()) {
                    timeOut--;
                    if (timeOut % 2 == 0)
                        System.out.println("Timeout in: " + timeOut/2);
                }
            }
            running = false;
        }).start();

//        Constantly listens for incoming packets
        while (running) {
            System.out.println("Listening for clients...");
            try {
                connectionListener();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        running = false;
    }

    private void sendWelcome() {
        TestPacket packet = new TestPacket();
        packet.msg = "Welcome to the server!";
        sendTo(packet, MULTICAST_ADDRESS, MULTICAST_PORT);
    }

    private void connectionListener() throws IOException {
        byte[] pokeBuf = new byte[objectToByteArray(new ConnectPacket()).length];
        DatagramPacket incPoke = new DatagramPacket(pokeBuf, pokeBuf.length);

        if (ClientPositionHandler.positions.isEmpty()) {
            clientSocket.setSoTimeout(10000);
        } else clientSocket.setSoTimeout(0);

        try {
            clientSocket.receive(incPoke);
        } catch (SocketTimeoutException e) {
            running = false;
            return;
        }

        /*************************************************/
        System.out.println("Packet from client " + incPoke.getAddress() + " received");
        /*************************************************/
        Object receivedPoke = byteArrayToObject(pokeBuf);

        listenerServer.received(receivedPoke, incPoke.getAddress(), incPoke.getPort());

        /*************************************************/
        System.out.println("Client's position added to the list");
        /*************************************************/
    }

    public static void addConnection(InetAddress address, int port) {
        ConnectedToServer connection = new ConnectedToServer(address, port);
        ClientPositionHandler.positions.put(address, connection.getPosition());
    }

    public void sendTo(Object o, String hostName, int desPort) {
        try {
            DatagramSocket dSocket = new DatagramSocket();
            InetAddress address = InetAddress.getByName(hostName);

//          Creates a byte array of the object
            byte[] sendBuf = objectToByteArray(o);

            DatagramPacket packet = new DatagramPacket(sendBuf, sendBuf.length, address, desPort);
            dSocket.send(packet);

        } catch (UnknownHostException e) {
            System.err.println("Exception:  " + e);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
