package com.halflife3.Networking.Server;

import com.halflife3.Model.Vector2;
import com.halflife3.Networking.Packets.*;

import java.io.*;
import java.net.*;
import java.util.Enumeration;
import java.util.HashMap;

public class Server implements Runnable {

    public static final String  MULTICAST_ADDRESS   = "239.255.42.99";
    public static final int     MULTICAST_PORT      = 5555;
    public static final int     LISTENER_PORT       = 5544;
    public static final int     GET_PORT_PORT       = 5566;
    public static final int     POSITIONS_PORT      = 5533;

    private boolean running = false;
    private static boolean welcoming = true;
    private PositionListPacket posPacket;
    private DatagramSocket clientSocket;
    private EventListenerServer listenerServer;
    public final int SERVER_TIMEOUT = 60000; // milliseconds
    private static int clientPort = 6000;
    private static HashMap<Vector2, Boolean> positionAvailable = new HashMap<>();
    private static Vector2[] startPositions = {new Vector2(80, 80),
                                               new Vector2(680, 80),
                                               new Vector2(80, 480),
                                               new Vector2(680, 480)};
    public static String[] botNames = new String[]{"bot0", "bot1", "bot2", "bot3"};

    public void start() {
//        Fills the positionList with 4 bot players, giving them available starting positions
        for (int i = 0; i < 4; i++) {
            Vector2 startPosition = startPositions[i];
            positionAvailable.put(startPosition, true);

            PositionPacket botPacket = new PositionPacket();
            botPacket.velX = 0;
            botPacket.velY = 0;
            botPacket.rotation = 0;
            botPacket.orgPosX = botPacket.spawnX = startPositions[i].getX();
            botPacket.orgPosY = botPacket.spawnY = startPositions[i].getY();

            ClientPositionHandlerServer.positionList.put(botNames[i], botPacket);
            ClientPositionHandlerServer.connectedIPs.add(botNames[i]);

        }

        try { clientSocket = new DatagramSocket(LISTENER_PORT); } catch (SocketException e) {
            e.printStackTrace();
        }

        posPacket = new PositionListPacket();
        listenerServer = new EventListenerServer();
        new Thread(this).start();
    }

    @Override
    public void run() {
        running = true;
        System.out.println("Multicasting on port: " + MULTICAST_PORT);

//        Multicasts WelcomePackets
        new Thread(() -> {
            int timeOut = SERVER_TIMEOUT/1000;
            while (running && timeOut > 0) {
                if (welcoming) {
                    multicastPacket(new WelcomePacket(), MULTICAST_PORT);
                }

                waitASecond();

                if (ClientPositionHandlerServer.clientList.isEmpty()) {
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
                if (System.nanoTime() - serverNanoTime > Math.round(1.0/60 * 1e9)) {
                    posPacket.posList = ClientPositionHandlerServer.positionList;
                    posPacket.connectedIPs = ClientPositionHandlerServer.connectedIPs;
                    multicastPacket(posPacket, POSITIONS_PORT);
                    serverNanoTime = System.nanoTime();
                }
            }
        }).start();

//        Listens for incoming packets
        while (running) {
            if (ClientPositionHandlerServer.clientList.size() < 4) {
                System.out.println("Listening for clients...");
            }
            try {
                connectionListener();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void connectionListener() throws IOException {
        byte[] pokeBuf = new byte[objectToByteArray(new ConnectPacket()).length];
        DatagramPacket incPoke = new DatagramPacket(pokeBuf, pokeBuf.length);

        if (ClientPositionHandlerServer.clientList.isEmpty()) {
            clientSocket.setSoTimeout(SERVER_TIMEOUT + 1000);
        } else clientSocket.setSoTimeout(0);

        try {
            clientSocket.receive(incPoke);
        } catch (SocketTimeoutException e) {
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
        if (ClientPositionHandlerServer.clientList.size() >= 4) {
            System.out.println("Server is full");
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
                ClientPositionHandlerServer.positionList.remove(botNames[i]);

//                Adds the player (with the [i]th startPosition) to the positionList
                PositionPacket playerPacket = new PositionPacket();
                playerPacket.rotation = 0;
                playerPacket.orgPosX = playerPacket.spawnX = startPosition.getX();
                playerPacket.orgPosY = playerPacket.spawnY = startPosition.getY();
                playerPacket.velX = 0;
                playerPacket.velY = 0;

                ClientPositionHandlerServer.positionList.put(address.toString(), playerPacket);

//                Disables the [i]th startPosition so that no new players could have it assigned to them
                positionAvailable.replace(startPosition, false);
                break;
            }
        }
        //endregion

        ConnectedToServer connection = new ConnectedToServer(address, clientPort, portPacket.getStartPosition());
        new Thread(connection).start();

        multicastPacket(portPacket, GET_PORT_PORT);

        ClientPositionHandlerServer.connectedIPs.add(address.toString());
        ClientPositionHandlerServer.clientList.put(address, connection);

        clientPort++;
        welcoming = true;
    }

    public static void removeConnection(InetAddress address) {
        if (ClientPositionHandlerServer.clientList.size() >= 4) {
            welcoming = true;
        }
        positionAvailable.replace(ClientPositionHandlerServer.clientList.get(address).getSpawnPoint(), true);

        double clientSpawnX = ClientPositionHandlerServer.positionList.get(address.toString()).spawnX;
        double clientSpawnY = ClientPositionHandlerServer.positionList.get(address.toString()).spawnY;

        //Removes the player from positionList and adds a bot in its stead
        for (int i = 0; i < 4; i++) {
            if (clientSpawnX == startPositions[i].getX()) {
                if (clientSpawnY == startPositions[i].getY()) {
                    ClientPositionHandlerServer.positionList.remove(address.toString());

                    PositionPacket botPacket = new PositionPacket();
                    botPacket.velX = 0;
                    botPacket.velY = 0;
                    botPacket.rotation = 0;
                    botPacket.orgPosX = botPacket.spawnX = startPositions[i].getX();
                    botPacket.orgPosY = botPacket.spawnY = startPositions[i].getY();

                    ClientPositionHandlerServer.positionList.put(botNames[i], botPacket);
                }
            }
        }

        ClientPositionHandlerServer.clientList.get(address).close();
        ClientPositionHandlerServer.clientList.remove(address);
        ClientPositionHandlerServer.connectedIPs.remove(address.toString());
        System.out.println(address + " has disconnected");
    }

    public static void multicastPacket(Object o, int mPort) {
        try {
            MulticastSocket multicastSocket = new MulticastSocket();

            //region Looks for the Wi-Fi adapter and sets the interface to it
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
            //endregion

            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            byte[] sendBuf = objectToByteArray(o);
            DatagramPacket packet = new DatagramPacket(sendBuf, sendBuf.length, group, mPort);
            multicastSocket.send(packet);

        } catch (UnknownHostException e) {
            System.err.println("Exception:  " + e);
            e.printStackTrace();
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

}
