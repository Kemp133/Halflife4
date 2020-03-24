package com.halflife3.Networking.Server;

import com.halflife3.Mechanics.AI.AI;
import com.halflife3.Mechanics.Vector2;
import com.halflife3.Networking.Packets.*;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;

public class Server implements Runnable {

    private final int PACKETS_PER_SECOND = 90;
    //region Variables
    public static final String  MULTICAST_ADDRESS   = "239.255.42.99";
    public static final int     MULTICAST_PORT      = 5555;
    public static final int     LISTENER_PORT       = 5544;
    public static final int     GET_PORT_PORT       = 5566;
    public static final int     POSITIONS_PORT      = 5533;

    private boolean running = false;
    private static boolean welcoming = true;
    private static InetAddress multicastGroup;
    private static MulticastSocket multicastSocket;
    private PositionListPacket posListPacket;
    private DatagramSocket clientSocket;
    private EventListenerServer listenerServer;
    public final int SERVER_TIMEOUT = 3000000; // milliseconds
    private static int clientPort = 6666;
    private static HashMap<Vector2, Boolean> positionAvailable = new HashMap<>();
    private static Vector2[] startPositions = {new Vector2(80, 480),
                                               new Vector2(80, 720),
                                               new Vector2(1920, 480),
                                               new Vector2(1920, 720)};
    public static ArrayList<String> botNamesList = new ArrayList<>(Arrays.asList("bot0", "bot1", "bot2", "bot3"));
    private AI botAI;
    //endregion

    public void start() {
        botAI = new AI();
        final boolean[] readyAI = {false};

        new Thread(() -> readyAI[0] = botAI.setupMap()).start();

//        Fills the positionList with 4 bot players, giving them available starting positions
        for (int i = 0; i < 4; i++) {
            Vector2 startPosition = startPositions[i];
            positionAvailable.put(startPosition, true);

            PositionPacket botPacket = new PositionPacket();
            botPacket.velX = 0;
            botPacket.velY = 0;
            botPacket.degrees = 0;
            botPacket.orgPosX = botPacket.spawnX = startPositions[i].getX();
            botPacket.orgPosY = botPacket.spawnY = startPositions[i].getY();

            ClientListServer.positionList.put(botNamesList.get(i), botPacket);
            ClientListServer.connectedIPs.add(botNamesList.get(i));

        }

        try {
            clientSocket = new DatagramSocket(LISTENER_PORT);
            multicastGroup = InetAddress.getByName(MULTICAST_ADDRESS);
            multicastSocket = new MulticastSocket();
            setWifiInterface();
        } catch (IOException e) { e.printStackTrace(); }

        posListPacket = new PositionListPacket();
        listenerServer = new EventListenerServer();

        while (!readyAI[0]) try { Thread.sleep(1); } catch (InterruptedException ignored) {}

//        moveAI();

        new Thread(this).start();
    }

    @Override
    public void run() {
        running = true;
        System.out.println("Multicasting on port: " + MULTICAST_PORT);
        System.out.println("Listening for clients...");

//        Multicasts WelcomePackets
        new Thread(() -> {
            int timeOut = SERVER_TIMEOUT/1000;
            while (running && timeOut > 0) {
                if (welcoming) { multicastPacket(new WelcomePacket(), MULTICAST_PORT); }

                waitASecond();

                if (ClientListServer.clientList.isEmpty()) {
                    if (timeOut <= 3)
                        System.out.println("Timeout in: " + timeOut);
                    timeOut--;
                } else timeOut = SERVER_TIMEOUT/1000;
            }
            running = false;
            welcoming = false;
            clientSocket.close();
        }).start();

//        Multicasts the positionList
        new Thread(() -> {
            double serverNanoTime = System.nanoTime();
            while (running) {
                if (System.nanoTime() - serverNanoTime > Math.round(1.0/PACKETS_PER_SECOND * 1e9)) {
//                    if (!ClientListServer.clientList.isEmpty() && ClientListServer.clientList.size() < 4)
//                        moveAI();

                    posListPacket.posList = ClientListServer.positionList;
                    posListPacket.connectedIPs = ClientListServer.connectedIPs;
                    multicastPacket(posListPacket, POSITIONS_PORT);
                    serverNanoTime = System.nanoTime();
                }
            }
        }).start();

//        Listens for incoming packets
        while (running) {
            if (ClientListServer.clientList.size() < 4) {
                try { connectionListener(); } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        multicastSocket.close();
    }

    private void moveAI() {
        for (var ip : ClientListServer.connectedIPs) {
            if (!botNamesList.contains(ip))
                continue;

            long before = System.currentTimeMillis();
            System.out.print(ip + ": ");
            EventListenerServer.replacing(ip, botAI.getBotMovement(ClientListServer.positionList.get(ip)));
            long after = System.currentTimeMillis();
            System.out.println("Time taken (ms): " + (after - before) + '\n');
//            break;
        }
    }

    private void connectionListener() throws IOException {
        byte[] pokeBuf = new byte[objectToByteArray(new ConnectPacket()).length];
        DatagramPacket incPoke = new DatagramPacket(pokeBuf, pokeBuf.length);

        if (ClientListServer.clientList.isEmpty()) {
            clientSocket.setSoTimeout(SERVER_TIMEOUT + 1000);
        } else clientSocket.setSoTimeout(0);

        try { clientSocket.receive(incPoke); }
        catch (SocketTimeoutException e) {
            running = false;
            return;
        } catch (SocketException e) {
            System.out.println("Server closed");
            return;
        }

        System.out.println(incPoke.getAddress() + " has connected");
        welcoming = false;

        Object receivedPoke = byteArrayToObject(pokeBuf);
        listenerServer.received(receivedPoke, incPoke.getAddress());
    }

    public static void addConnection(InetAddress address) {
        //region Checks if Server is full
        if (ClientListServer.clientList.size() >= 4) {
            System.out.println("Server is full");
            welcoming = false;
            return;
        }
        //endregion

        UniquePortPacket portPacket = new UniquePortPacket();
        //region Client's UniqueInfo and new positionList entry
        portPacket.setPort(clientPort);
        portPacket.setClientAddress(address);
        for (int i = 0; i < 4; i++) {
            Vector2 startPosition = startPositions[i];
            if (positionAvailable.get(startPosition)) {
//                Sets the start position for the UniqueInfo packet
                portPacket.setStartPosition(startPosition);

//                Removes the bot holding the [i]th startPosition
                ClientListServer.positionList.remove(botNamesList.get(i));

                //region Adds the player (with the [i]th startPosition) to the positionList
                PositionPacket playerPacket = new PositionPacket();
                playerPacket.degrees = 0;
                playerPacket.orgPosX = playerPacket.spawnX = startPosition.getX();
                playerPacket.orgPosY = playerPacket.spawnY = startPosition.getY();
                playerPacket.velX = 0;
                playerPacket.velY = 0;
                ClientListServer.positionList.put(address.toString(), playerPacket);
                //endregion

                ClientListServer.connectedIPs.remove(botNamesList.get(i));
                ClientListServer.connectedIPs.add(address.toString());

//                Disables the [i]th startPosition so that no new players could have it assigned to them
                positionAvailable.replace(startPosition, false);
                break;
            }
        }
        //endregion

        ConnectedToServer connection = new ConnectedToServer(address, clientPort, portPacket.getStartPosition());
        new Thread(connection).start();

        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
        multicastPacket(portPacket, GET_PORT_PORT);

        ClientListServer.clientList.put(address, connection);

        clientPort++;
        welcoming = true;
    }

    public static void removeConnection(InetAddress address) {
        if (ClientListServer.clientList.size() >= 4) {
            welcoming = true;
        }
        positionAvailable.replace(ClientListServer.clientList.get(address).getSpawnPoint(), true);

        double clientSpawnX = ClientListServer.positionList.get(address.toString()).spawnX;
        double clientSpawnY = ClientListServer.positionList.get(address.toString()).spawnY;

        //Removes the player from positionList and adds a bot in its stead
        for (int i = 0; i < 4; i++) {
            if (clientSpawnX == startPositions[i].getX()) {
                if (clientSpawnY == startPositions[i].getY()) {
                    ClientListServer.positionList.remove(address.toString());

                    PositionPacket botPacket = new PositionPacket();
                    botPacket.velX = 0;
                    botPacket.velY = 0;
                    botPacket.degrees = 0;
                    botPacket.orgPosX = botPacket.spawnX = startPositions[i].getX();
                    botPacket.orgPosY = botPacket.spawnY = startPositions[i].getY();

                    ClientListServer.positionList.put(botNamesList.get(i), botPacket);
                    ClientListServer.connectedIPs.remove(address.toString());
                    ClientListServer.connectedIPs.add(botNamesList.get(i));
                }
            }
        }

        ClientListServer.clientList.get(address).close();
        ClientListServer.clientList.remove(address);
        System.out.println(address + " has disconnected");
    }

    public synchronized static void multicastPacket(Object o, int mPort) {
        try {
            byte[] sendBuf = objectToByteArray(o);
            DatagramPacket packet = new DatagramPacket(sendBuf, sendBuf.length, multicastGroup, mPort);
            multicastSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void waitASecond() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

    public static void setWifiInterface() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface net = interfaces.nextElement();
                if (!net.getName().startsWith("wlan") || !net.isUp())
                    continue;

                Enumeration<InetAddress> addresses = net.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr.toString().length() < 17) {
                        multicastSocket.setInterface(addr);
                        return;
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
