package com.halflife3.Networking.Server;

import com.halflife3.Mechanics.AI.AI;
import com.halflife3.Mechanics.GameObjects.BasicBall;
import com.halflife3.Mechanics.GameObjects.Bricks;
import com.halflife3.Mechanics.Vector2;
import com.halflife3.Networking.Client.ClientGame;
import com.halflife3.Networking.Packets.*;
import com.halflife3.View.MapRender;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
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
    private final int GOAL_WIDTH = 4;
    private static int clientPort = 6666;
    private static HashMap<Vector2, Boolean> positionAvailable = new HashMap<>();
    private static Vector2[] startPositions = {new Vector2(80, 480),
                                               new Vector2(80, 720),
                                               new Vector2(1920, 480),
                                               new Vector2(1920, 720)};
    public static ArrayList<String> botNamesList = new ArrayList<>(Arrays.asList("bot0", "bot1", "bot2", "bot3"));
    private AI botAI;
    private BasicBall theBall;
    private PositionPacket ballPacket;
    private int mapWidth;
    private int mapHeight;
    //endregion

    public void start() {
        botAI = new AI();
        final boolean[] readyAI = {false};

        new Thread(() -> readyAI[0] = botAI.setupMap()).start();

        //region Fills the positionList with 4 bot players, giving them available starting positions
        for (int i = 0; i < 4; i++) {
            positionAvailable.put(startPositions[i], true);

            PositionPacket botPacket = new PositionPacket();
            botPacket.velX = 0;
            botPacket.velY = 0;
            botPacket.degrees = 0;
            botPacket.posX = botPacket.spawnX = startPositions[i].getX();
            botPacket.posY = botPacket.spawnY = startPositions[i].getY();

            ClientListServer.positionList.put(botNamesList.get(i), botPacket);
            ClientListServer.connectedIPs.add(botNamesList.get(i));
        }
        //endregion

        //region Adds the ball to the positionList
        try {
            BufferedImage mapImage = ImageIO.read(new File("res/map.png"));
            int mapWidthMiddle = mapImage.getWidth() * 20;
            int mapHeightMiddle = mapImage.getHeight() * 20;
            mapWidth = mapWidthMiddle * 2;
            mapHeight = mapHeightMiddle * 2;

            theBall = new BasicBall(new Vector2(mapWidthMiddle, mapHeightMiddle), new Vector2(0, 0));

            ballPacket = new PositionPacket();
            ballPacket.velX = 0;
            ballPacket.velY = 0;
            ballPacket.posX = ballPacket.spawnX = mapWidthMiddle;
            ballPacket.posY = ballPacket.spawnY = mapHeightMiddle;
            ballPacket.degrees = 0;
            ballPacket.holdsBall = false;

            ClientListServer.positionList.put("ball", ballPacket);
        } catch (IOException e) { e.printStackTrace(); }
        //endregion

        //region Sets up the communication sockets
        try {
            clientSocket = new DatagramSocket(LISTENER_PORT);
            multicastGroup = InetAddress.getByName(MULTICAST_ADDRESS);
            multicastSocket = new MulticastSocket();
            setWifiInterface();
        } catch (IOException e) { e.printStackTrace(); }
        //endregion

        //region Loads the map
        MapRender.LoadLevel();
        //endregion

        posListPacket = new PositionListPacket();
        listenerServer = new EventListenerServer();

//        Wait until the AI is done loading the map
        while (!readyAI[0]) try { Thread.sleep(1); } catch (InterruptedException ignored) {}

//        moveAI(0);

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
            long lastUpdate = System.nanoTime();
//            int fpsCounter = 0;
//            double second = 1.0;
            double elapsedTime;

            while (running) {
                if (System.nanoTime() - lastUpdate < 1e9 / PACKETS_PER_SECOND)
                    continue;

                //region Calculate time since last update.
                elapsedTime = (System.nanoTime() - lastUpdate) / 1e9;
                lastUpdate = System.nanoTime();
                //endregion

                gameFrame(elapsedTime);

                //region FPS counter
//                second -= elapsedTime;
//                fpsCounter++;
//                if (second < 0) {
//                    System.out.println("FPS: " + fpsCounter);
//                    second = 1;
//                    fpsCounter = 0;
//                }
                //endregion
            }
        }).start();

//        Listens for incoming packets
        while (running) {
            if (ClientListServer.clientList.size() < 4) {
                try {
                    connectionListener();
                } catch (IOException e) { e.printStackTrace(); }
            }
        }

        multicastSocket.close();
    }

    private void gameFrame(double elapsedTime) {
        //region Move AI controlled players
//        if (!ClientListServer.clientList.isEmpty() && ClientListServer.clientList.size() < 4)
//            moveAI(elapsedTime);
        //endregion

        //region Ball's position and velocity
        for (String ip : ClientListServer.positionList.keySet()) {
            PositionPacket playerWithBall = ClientListServer.positionList.get(ip);

            if (!playerWithBall.holdsBall)
                continue;

            //region Ball's position
            double degreeRadians = Math.toRadians(playerWithBall.degrees);
            double ballX = Math.cos(degreeRadians);
            double ballY = Math.sin(degreeRadians);
            Vector2 ballDir = new Vector2(ballX * 35, ballY * 35);
            Vector2 ballPos = new Vector2(playerWithBall.posX + 6, playerWithBall.posY + 6).add(ballDir);
            theBall.setPosition(ballPos);
            theBall.isHeld = true;
            //endregion

            if (!playerWithBall.bulletShot)
                continue;

            //region Ball's velocity if it's been shot
            Vector2 shotVel = new Vector2(ballX, ballY).multiply(ClientGame.SHOT_SPEED);
            theBall.setVelocity(new Vector2(shotVel).multiply(1.5));
            theBall.setAcceleration(new Vector2(shotVel).divide(100));
            theBall.isHeld = false;
            playerWithBall.bulletShot = false;
            playerWithBall.holdsBall = false;
            EventListenerServer.replaceEntry(ip, playerWithBall);
            //endregion

            break;
        }
        //endregion

        //region Ball collision
        if (!theBall.isHeld) ballWallBounce();
        //endregion

        //region Update objects
        theBall.update(elapsedTime);
        //endregion

        //region Update ballPacket
        ballPacket.posX = theBall.getPosX();
        ballPacket.posY = theBall.getPosY();
        ballPacket.velX = theBall.getVelX();
        ballPacket.velY = theBall.getVelY();
        EventListenerServer.replaceEntry("ball", ballPacket);
        //endregion

        //region Check if a goal has been scored
        if (theBall.getPosX() > mapWidth - GOAL_WIDTH * 40 || theBall.getPosX() < GOAL_WIDTH * 40) {
            theBall.setPosition(new Vector2(mapWidth/2f,mapHeight/2f));
            theBall.resetVelocity();
        }
        //endregion

        //region Sends the position list packet to all clients
        posListPacket.posList = ClientListServer.positionList;
        posListPacket.connectedIPs = ClientListServer.connectedIPs;
        multicastPacket(posListPacket, POSITIONS_PORT);
        //endregion
    }

    private void ballWallBounce() {
        for (Bricks block : MapRender.GetList()) {
            if (!theBall.getBounds().intersects(block.getBounds().getBoundsInLocal()))
                continue;

            Vector2 brickCenter = new Vector2(block.getPosX() + block.getWidth() / 2,
                    block.getPosY() + block.getHeight() / 2);
            Vector2 ballCenter = new Vector2(theBall.getPosX() + theBall.getWidth() / 2,
                    theBall.getPosY() + theBall.getHeight() / 2);

            Vector2 relevantPos = new Vector2(ballCenter).subtract(brickCenter);
            double rel_x = relevantPos.getX();
            double rel_y = relevantPos.getY();

            if ((rel_x > 0 && rel_x < 38 && rel_y > -33 && rel_y < 33 && theBall.getVelX() < 0) ||
                    rel_x > -38 && rel_x < 0 && rel_y > -33 && rel_y < 33 && theBall.getVelX() > 0) {
                theBall.collision(1);
            } else if ((rel_x > -33 && rel_x < 33 && rel_y > 0 && rel_y < 38 && theBall.getVelY() < 0) ||
                    rel_x > -33 && rel_x < 33 && rel_y > -38 && rel_y < 0 && theBall.getVelY() > 0) {
                theBall.collision(2);
            }
        }
    }

    private void moveAI(double time) {
        for (var ip : ClientListServer.connectedIPs) {
            if (!botNamesList.contains(ip))
                continue;

            long before = System.currentTimeMillis();
            System.out.print(ip + ": ");
            EventListenerServer.replaceEntry(ip, botAI.getBotMovement(ClientListServer.positionList.get(ip)));
            long after = System.currentTimeMillis();
            System.out.println("Time taken (ms): " + (after - before) + '\n');
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
                playerPacket.posX = playerPacket.spawnX = startPosition.getX();
                playerPacket.posY = playerPacket.spawnY = startPosition.getY();
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
                    botPacket.posX = botPacket.spawnX = startPositions[i].getX();
                    botPacket.posY = botPacket.spawnY = startPositions[i].getY();

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
