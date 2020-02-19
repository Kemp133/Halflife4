package com.halflife3.Networking.Client;

import com.halflife3.Model.Vector2;
import com.halflife3.Networking.Packets.ConnectPacket;
import com.halflife3.Networking.Packets.DisconnectPacket;
import com.halflife3.Networking.Packets.WelcomePacket;
import com.halflife3.Networking.Server.Server;

import java.io.*;
import java.net.*;
import java.util.Enumeration;

import static com.halflife3.Networking.Server.Server.GET_PORT_PORT;

public class Client implements Runnable {

    //region Variables
//    For "catching" the server
    protected static MulticastSocket serverSocket = null;
    protected InetAddress group = null;

//    For receiving clients' positions
    protected static MulticastSocket positionSocket = null;

    //    For sending packets to the server
    private static InetAddress hostAddress;
    private static DatagramSocket outSocket;

//    Client's data
    public static InetAddress clientAddress;
    private static int uniquePort;
    private EventListenerClient listenerClient;
    public static Vector2 startingPosition;

//isRunning
    private static boolean running = false;
    //endregion

//    Joins the multicast group to listen for multicasted packets
    public void joinGroup() {
        try {
            serverSocket = new MulticastSocket(Server.MULTICAST_PORT);
            positionSocket = new MulticastSocket(Server.POSITIONS_PORT);
            group = InetAddress.getByName(Server.MULTICAST_ADDRESS);

            //region Sets interface to Wi-Fi and gets the IP address
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface net = interfaces.nextElement();
                if (!net.getName().startsWith("wlan"))
                    continue;

                Enumeration<InetAddress> addresses = net.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr.toString().length() < 17) {
                        serverSocket.setInterface(addr);
                        positionSocket.setInterface(addr);
                        clientAddress = addr;
                    }
                }
            }
            //endregion

            serverSocket.joinGroup(group);
            positionSocket.joinGroup(group);

            System.out.println("Joined group: " + group.getHostName() + " with address: " + clientAddress.toString());
        } catch (ConnectException e) {
            System.out.println("Unable to join the group");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    Gets the server's IP address
    public void getHostInfo() {
        try {
//            Receives the Welcome packet
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

//    Connects to the server and gets a port to output to
    public void start() {
        listenerClient = new EventListenerClient();

//        Lets the server know the client has connected
        ConnectPacket join = new ConnectPacket();
        sendPacket(join, Server.LISTENER_PORT);

//        Gets the unique port to communicate with the server
        getUniqueInfo();

        running = true;
        new Thread(this).start();
    }

    @Override
    public void run() {
        while(running) {
            System.out.println("Client running");
            for (int i = 0; i < 300 && running; i++) {
                waitASecond();
            }
        }
        System.out.println("Client stopped running");
    }

//    Sends a disconnect packet to the server and closes the sockets
    public static void close() {
        running = false;

        //region Sends Disconnect packet to the Server
        DisconnectPacket leave = new DisconnectPacket();
        byte[] tempBuf = objectToByteArray(leave);

        DatagramPacket dc = new DatagramPacket(tempBuf, tempBuf.length, hostAddress, uniquePort);
        try {
            outSocket.send(dc);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //endregion

        serverSocket.close();
        outSocket.close();
    }

//    Gets the unique port to communicate with the server and a starting position
    public void getUniqueInfo() {
        try {
            serverSocket = new MulticastSocket(GET_PORT_PORT);
            serverSocket.setInterface(clientAddress);
            serverSocket.joinGroup(group);

            receivePacket();

            serverSocket = new MulticastSocket(Server.MULTICAST_PORT);
            serverSocket.setInterface(clientAddress);
            serverSocket.joinGroup(group);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Unique Client Port: " + uniquePort);
    }

//    Receives and sorts a packet
    public void receivePacket() {
        try {
            byte[] recBuf = new byte[5000];
            DatagramPacket packet = new DatagramPacket(recBuf, recBuf.length);
            serverSocket.receive(packet);
            Object o = byteArrayToObject(recBuf);
            listenerClient.received(o);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receivePositions() {
        try {
            byte[] recBuf = new byte[5000];
            DatagramPacket packet = new DatagramPacket(recBuf, recBuf.length);
            positionSocket.receive(packet);
            Object o = byteArrayToObject(recBuf);
            listenerClient.received(o);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    Sends a packet to the Server
    public static void sendPacket(Object objectToSend, int port) {
        byte[] tempBuf = objectToByteArray(objectToSend);
        DatagramPacket packet = new DatagramPacket(tempBuf, tempBuf.length, hostAddress, port);
        try {
            outSocket = new DatagramSocket();
            outSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    Makes the Thread sleep for 1 second
    private void waitASecond() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

//    Converts an object (packet) into a byte array
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

//    Converts a byte array into an object (packet)
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

    public InetAddress getClientAddress() { return clientAddress; }

    public Vector2 getStartingPosition() {
        return startingPosition;
    }

    public static int getUniquePort() {
        return uniquePort;
    }

    public static void setUniquePort(int uniquePort) {
        Client.uniquePort = uniquePort;
    }
}
