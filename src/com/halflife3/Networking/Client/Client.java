package com.halflife3.Networking.Client;

import com.halflife3.Mechanics.Vector2;
import com.halflife3.Networking.NetworkingUtilities;
import com.halflife3.Networking.Packets.ConnectPacket;
import com.halflife3.Networking.Packets.DisconnectPacket;
import com.halflife3.Networking.Packets.PositionListPacket;
import com.halflife3.Networking.Packets.WelcomePacket;
import com.halflife3.Networking.Server.Server;

import java.io.*;
import java.net.*;
import java.util.Enumeration;

import static com.halflife3.Networking.Server.Server.GET_PORT_PORT;

public class Client {

    //region Variables
//    For "catching" the server
    protected static MulticastSocket serverSocket = null;
    protected InetAddress group = null;

//    For receiving clients' positions
    protected static MulticastSocket positionSocket = null;
    private static int incPacketSize = 2000;

    //    For sending packets to the server
    private static InetAddress hostAddress;
    private static DatagramSocket outSocket;

//    Client's data
    public static InetAddress clientAddress;
    private static int uniquePort;
    private static EventListenerClient listenerClient;
    public static Vector2 startingPosition;

//    List of Clients
    public static PositionListPacket listOfClients;
    //endregion

//    Joins the multicast group to listen for multicasted packets
    public void joinGroup() {
        try {
            System.out.println("Searching for a multicast group...");
            group = InetAddress.getByName(Server.MULTICAST_ADDRESS);

            serverSocket = new MulticastSocket(Server.MULTICAST_PORT);
            positionSocket = new MulticastSocket(Server.POSITIONS_PORT);

            clientAddress = NetworkingUtilities.setWifiInterface();
            serverSocket.setInterface(clientAddress);
            positionSocket.setInterface(clientAddress);

            serverSocket.joinGroup(group);
            positionSocket.joinGroup(group);

            System.out.println("Joined group: " + Server.MULTICAST_ADDRESS + " with address: " + clientAddress.toString());
        } catch (ConnectException e) {
            System.out.println("Unable to join the group");
            NetworkingUtilities.CreateErrorMessage(
                    "Unable To Join The Group",
                    "The connection to the group was unsuccessful",
                    "Message: " + e.getMessage()
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    Gets the server's IP address
    public void getHostInfo() {
        try {
//            Receives the Welcome packet
            byte[] firstBuf = new byte[NetworkingUtilities.objectToByteArray(new WelcomePacket()).length];
            DatagramPacket firstPacket = new DatagramPacket(firstBuf, firstBuf.length);
            System.out.println("Looking for host...");
            serverSocket.receive(firstPacket);

//            Gets the server's address
            hostAddress = firstPacket.getAddress();

            new Thread(() -> {
                try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
                System.out.println("Host found: " + hostAddress);
                System.out.println("Waiting for unique port...");
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    Connects to the server and gets a port to output to
    public void start() {
        listenerClient = new EventListenerClient();

//        Lets the server know the client has connected
        sendPacket(new ConnectPacket(), Server.LISTENER_PORT);

//        Gets the unique port to communicate with the server
        getUniqueInfo();

        System.out.println("Client connection set up. Starting game...");
    }

//    Sends a disconnect packet to the server and closes the sockets
    public static void disconnect() {

        //region Sends Disconnect packet to the Server
        DisconnectPacket leave = new DisconnectPacket();
        byte[] tempBuf = NetworkingUtilities.objectToByteArray(leave);

        DatagramPacket dc = new DatagramPacket(tempBuf, tempBuf.length, hostAddress, uniquePort);
        try { outSocket.send(dc); } catch (IOException e) {
            e.printStackTrace();
        }
        //endregion

        serverSocket.close();
        outSocket.close();
        positionSocket.close();
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
            byte[] recBuf = new byte[3000];
            DatagramPacket packet = new DatagramPacket(recBuf, recBuf.length);
            serverSocket.receive(packet);
            Object o = NetworkingUtilities.byteArrayToObject(recBuf);
            listenerClient.received(o);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    Updates the 'listOfClients' variable
    public static void receivePositions() {
        try {
            byte[] recBuf = new byte[incPacketSize];
            DatagramPacket packet = new DatagramPacket(recBuf, recBuf.length);
            positionSocket.receive(packet);
            Object o = NetworkingUtilities.byteArrayToObject(recBuf);
            if (incPacketSize == 2000) incPacketSize = NetworkingUtilities.objectToByteArray(o).length + 100;
            listenerClient.received(o);
        } catch (SocketException ignored) {

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    Sends a packet to the Server
    public static void sendPacket(Object objectToSend, int port) {
        byte[] tempBuf = NetworkingUtilities.objectToByteArray(objectToSend);
        DatagramPacket packet = new DatagramPacket(tempBuf, tempBuf.length, hostAddress, port);
        try {
            outSocket = new DatagramSocket();
            outSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //region Old objectToByteArray Method (just in case)
    //    Converts a byte array into an object (packet)
//    private static Object byteArrayToObject(byte[] buf) {
//        Object o = null;
//
//        try {
//            ByteArrayInputStream byteStream = new ByteArrayInputStream(buf);
//            ObjectInputStream instream = new ObjectInputStream(new BufferedInputStream(byteStream));
//            o = instream.readObject();
//            instream.close();
//        } catch (IOException | ClassNotFoundException e) {
//            incPacketSize = 2000;
//            e.printStackTrace();
//        }
//
//        return o;
//    }
    //endregion

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
