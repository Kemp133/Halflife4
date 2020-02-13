package com.halflife3.Networking.Server;

import com.halflife3.Networking.Packets.ConnectPacket;
import com.halflife3.Networking.Packets.WelcomePacket;

import java.io.*;
import java.net.*;

public class Server implements Runnable {

    public static final int     MULTICAST_PORT      = 5555;
    public static final String  MULTICAST_ADDRESS   = "239.255.42.99";
    public static final int     LISTENER_PORT       = 5544;

    private boolean running = false;
    private DatagramSocket clientSocket;
    private EventListenerServer listenerServer;
    private final int SERVER_TIMEOUT = 60000; // milliseconds
    private static int clientPort = 6000;

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

//        Multicasts WelcomePackets, closes server after 60s of no connections
        new Thread(() -> {
            int timeOut = SERVER_TIMEOUT/1000;
            while (running && timeOut > 0) {
                sendWelcome();
                waitASecond();

                if (ClientPositionHandler.clientList.isEmpty()) {
                    if (timeOut <= 3)
                        System.out.println("Timeout in: " + timeOut);
                    timeOut--;
                }
            }
            running = false;
            clientSocket.close();
        }).start();

//        Listens for incoming packets
        while (running) {
            System.out.println("Listening for clients...");
            try {
                connectionListener();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendWelcome() {
        WelcomePacket packet = new WelcomePacket();
        packet.msg = "Welcome to the server!";
        sendTo(packet, MULTICAST_ADDRESS, MULTICAST_PORT);
    }

    private void connectionListener() throws IOException {
        byte[] pokeBuf = new byte[objectToByteArray(new ConnectPacket()).length];
        DatagramPacket incPoke = new DatagramPacket(pokeBuf, pokeBuf.length);

        if (ClientPositionHandler.clientList.isEmpty()) {
            clientSocket.setSoTimeout(SERVER_TIMEOUT + 1000);
        } else clientSocket.setSoTimeout(0);

        try {
            clientSocket.receive(incPoke);
        } catch (SocketTimeoutException e) {
            running = false;
            return;
        }

        System.out.println("Packet from client " + incPoke.getAddress() + " received");

        Object receivedPoke = byteArrayToObject(pokeBuf);
        listenerServer.received(receivedPoke, incPoke.getAddress());
    }

    public static void addConnection(InetAddress address) {
        ConnectedToServer connection = new ConnectedToServer(address, clientPort);
        new Thread(connection).start();
        ClientPositionHandler.clientList.put(address, connection);
        clientPort++;
    }

    public static void removeConnection(InetAddress address) {
        ClientPositionHandler.clientList.get(address).close();
        ClientPositionHandler.clientList.remove(address);
    }

    public void sendTo(Object o, String mAddress, int mPort) {
        try {
            MulticastSocket dSocket = new MulticastSocket();
            InetAddress group = InetAddress.getByName(mAddress);

//          Creates a byte array of the object
            byte[] sendBuf = objectToByteArray(o);

            DatagramPacket packet = new DatagramPacket(sendBuf, sendBuf.length, group, mPort);
            dSocket.send(packet);

        } catch (UnknownHostException e) {
            System.err.println("Exception:  " + e);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
