package com.halflife3.Networking.Server;

import com.halflife3.Networking.NetworkingUtilities;
import com.halflife3.Networking.Packets.ConnectPacket;
import com.halflife3.Networking.Packets.UniquePortPacket;
import com.halflife3.Networking.Packets.WelcomePacket;

import java.io.*;
import java.net.*;
import java.util.Enumeration;

public class Server implements Runnable {

    public static final int     MULTICAST_PORT      = 5555;
    public static final String  MULTICAST_ADDRESS   = "239.255.42.99";
    public static final int     LISTENER_PORT       = 5544;
    public static final int     GET_PORT_PORT       = 5566;

    private boolean running = false;
    private static boolean welcoming = true;
    private DatagramSocket clientSocket;
    private EventListenerServer listenerServer;
    public final int SERVER_TIMEOUT = 60000; // milliseconds
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
            WelcomePacket packet = new WelcomePacket();
            packet.msg = "Welcome to the server!";
            while (running && timeOut > 0) {
                if (welcoming) { multicastPacket(packet, MULTICAST_PORT); }
                NetworkingUtilities.waitASecond();

                if (ClientPositionHandlerServer.clientList.isEmpty()) {
                    if (timeOut <= 3)
                        System.out.println("Timeout in: " + timeOut);
                    timeOut--;
                }
            }
            running = false;
            welcoming = false;
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

    private void connectionListener() throws IOException {
        byte[] pokeBuf = new byte[NetworkingUtilities.objectToByteArray(new ConnectPacket()).length];
        DatagramPacket incPoke = new DatagramPacket(pokeBuf, pokeBuf.length);

        if (ClientPositionHandlerServer.clientList.isEmpty()) {
            clientSocket.setSoTimeout(SERVER_TIMEOUT + 1000);
        } else clientSocket.setSoTimeout(0);

        try {
            clientSocket.receive(incPoke);
        } catch (SocketTimeoutException e) {
            running = false;
            return;
        }

        System.out.println(incPoke.getAddress() + " has connected");
        welcoming = false;

        Object receivedPoke = NetworkingUtilities.byteArrayToObject(pokeBuf);
        listenerServer.received(receivedPoke, incPoke.getAddress());
    }

    public static void addConnection(InetAddress address) {
        ConnectedToServer connection = new ConnectedToServer(address, clientPort);
        new Thread(connection).start();

        UniquePortPacket portPacket = new UniquePortPacket();
        portPacket.setPort(clientPort);
        portPacket.setClientAddress(address);
        multicastPacket(portPacket, GET_PORT_PORT);

        ClientPositionHandlerServer.clientList.put(address, connection);

        clientPort++;
        welcoming = true;
    }

    public static void removeConnection(InetAddress address) {
        ClientPositionHandlerServer.clientList.get(address).close();
        ClientPositionHandlerServer.clientList.remove(address);
    }

    public static void multicastPacket(Object o, int mPort) {
        try {
            MulticastSocket multicastSocket = new MulticastSocket();

//            Looks for the Wi-Fi adapter and sets the interface to it
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface net = interfaces.nextElement();
                if (!net.getName().startsWith("wlan"))
                    continue;

                Enumeration<InetAddress> addresses = net.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr.toString().length() < 17) {
                        multicastSocket.setInterface(addr);
                    }
                }
            }

            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);

//          Creates a byte array of the object
            byte[] sendBuf = NetworkingUtilities.objectToByteArray(o);

            DatagramPacket packet = new DatagramPacket(sendBuf, sendBuf.length, group, mPort);
            multicastSocket.send(packet);

        } catch (UnknownHostException e) {
            System.err.println("Exception:  " + e);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}