package com.halflife3.Networking.Server;

import com.halflife3.Mechanics.AI.AI;
import com.halflife3.Mechanics.GameObjects.BasicBall;
import com.halflife3.Mechanics.GameObjects.Bricks;
import com.halflife3.Mechanics.Vector2;
import com.halflife3.Networking.Client.ClientGame;
import com.halflife3.Networking.NetworkingUtilities;
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
    //endregion

    public void start() {
        botAI = new AI();
        final boolean[] readyAI = {false};

        new Thread(() -> readyAI[0] = botAI.setupMap()).start();

        //region Fills the positionList with 4 bot players, giving them available starting positions
        for (int i = 0; i < 4; i++) {
            positionAvailable.put(startPositions[i], true);
            PositionPacket botPacket = newBotPacket(i);
            ClientListServer.positionList.put(botNamesList.get(i), botPacket);
            ClientListServer.connectedIPs.add(botNamesList.get(i));
        }
        //endregion

        //region Adds the ball to the positionList
        try {
            BufferedImage mapImage = ImageIO.read(new File("res/map.png"));
            int mapWidthMiddle = mapImage.getWidth() * 20;
            int mapHeightMiddle = mapImage.getHeight() * 20;

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
            multicastSocket.setInterface(NetworkingUtilities.setWifiInterface());
        } catch (SocketException e) {
            NetworkingUtilities.CreateErrorMessage(
                    "Error Setting Network Interface",
                    "Network interface could not be set",
                    e.getMessage()
            );
        }
        catch (IOException e) { e.printStackTrace(); }
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
            int fpsCounter = 0;
            double second = 1.0;
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
                try { connectionListener(); } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        multicastSocket.close();
    }

    private void gameFrame(double elapsedTime) {
//        if (!ClientListServer.clientList.isEmpty() && ClientListServer.clientList.size() < 4)
//            moveAI(elapsedTime);

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

        if (!theBall.isHeld) ballWallBounce();

        theBall.update(elapsedTime);

        ballPacket.posX = theBall.getPosX();
        ballPacket.posY = theBall.getPosY();
        ballPacket.velX = theBall.getVelX();
        ballPacket.velY = theBall.getVelY();
        EventListenerServer.replaceEntry("ball", ballPacket);

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
        byte[] pokeBuf = new byte[NetworkingUtilities.objectToByteArray(new ConnectPacket()).length];
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

        Object receivedPoke = NetworkingUtilities.byteArrayToObject(pokeBuf);
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

                    PositionPacket botPacket = newBotPacket(i);

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

    /**
     * A method to create a PositionPacket for a new bot
     * @param index The index of the start position to get the position values of
     * @return The newly created {@code PositionPacket}
     */
    public static PositionPacket newBotPacket(int index) {
        PositionPacket pp = new PositionPacket();
        pp.velX = 0;
        pp.velY = 0;
        pp.degrees = 0;
        pp.posX = pp.spawnX = startPositions[index].getX();
        pp.posY = pp.spawnY = startPositions[index].getY();
        return pp;
    }

    public synchronized static void multicastPacket(Object o, int mPort) {
        try {
            byte[] sendBuf = NetworkingUtilities.objectToByteArray(o);
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
}
