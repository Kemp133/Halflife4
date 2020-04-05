package com.halflife3.Networking.Server;

import com.halflife3.Controller.GameModes.MainMode;
import com.halflife3.GameUI.Maps;
import com.halflife3.Mechanics.AI.AI;
import com.halflife3.Mechanics.GameObjects.Ball;
import com.halflife3.Mechanics.GameObjects.Bricks;
import com.halflife3.Mechanics.Vector2;
import com.halflife3.Networking.Client.ClientGame;
import com.halflife3.Networking.NetworkingUtilities;
import com.halflife3.Networking.Packets.*;
import com.halflife3.View.MapRender;
import javafx.scene.image.Image;
import javafx.scene.shape.Circle;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Server implements Runnable {

    private final int PACKETS_PER_SECOND = 90;
    //region Variables
    public static final String  MULTICAST_ADDRESS   = "239.255.42.99";
    public static final int     MULTICAST_PORT      = 5555;
    public static final int     LISTENER_PORT       = 5544;
    public static final int     GET_PORT_PORT       = 5566;
    public static final int     POSITIONS_PORT      = 5533;
    public static final int     GOAL_WIDTH          = 4 * 40;
    public final int            SERVER_TIMEOUT      = 3000000; // milliseconds

    private boolean running = false;
    private static boolean welcoming = true;
    private static InetAddress multicastGroup;
    private static MulticastSocket multicastSocket;
    private PositionListPacket posListPacket;
    private DatagramSocket clientSocket;
    private EventListenerServer listenerServer;
    private static int clientPort = 6666;
    private static HashMap<Vector2, Boolean> availablePositions;
    public static Vector2[] startPositions = {new Vector2(160, 600),
                                               new Vector2(1840, 600)};
    public static ArrayList<String> botNamesList = new ArrayList<>(Arrays.asList("bot1", "bot2"));
    private HashMap<String, Circle> botCircles;
    private HashMap<String, PositionPacket> soughtPositions;
    private HashMap<String, PositionPacket> nextPositions;
    private HashMap<String, Boolean> alreadyLooking;
    private AI botAI;
    private Ball theBall;
    private Image playerImage;
    private PositionPacket ballPacket;
    private int mapWidth;
    private int mapHeight;
    private Vector2 previousBallVel;
    //endregion

    public void start() {
        //region Object initialisation
        botAI = new AI();
        botCircles = new HashMap<>();
        alreadyLooking = new HashMap<>();
        soughtPositions = new HashMap<>();
        nextPositions = new HashMap<>();
        availablePositions = new HashMap<>();
        previousBallVel = new Vector2(0,0);
        posListPacket = new PositionListPacket();
        listenerServer = new EventListenerServer();
        try (var fis = new FileInputStream("res/Sprites/PlayerSkins/Cosmo_Hovering.png")) {
            playerImage = new Image(fis);
        } catch (IOException e) { System.err.println("Player sprite not found!"); }
        //endregion

        final boolean[] readyAI = {false};
        new Thread(() -> readyAI[0] = botAI.setupMap()).start();

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

        //region Adds the ball to the positionList
        try {
            BufferedImage mapImage = ImageIO.read(new File(Maps.Map));
            int mapWidthMiddle = mapImage.getWidth() * 20;
            int mapHeightMiddle = mapImage.getHeight() * 20;
            mapWidth = mapWidthMiddle * 2;
            mapHeight = mapHeightMiddle * 2;

            theBall = new Ball(new Vector2(mapWidthMiddle, mapHeightMiddle), new Vector2(0, 0));

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

        //region Fills the positionList with bot players, giving them available starting positions
        for (int i = 0; i < startPositions.length; i++) {
            availablePositions.put(startPositions[i], true);
            PositionPacket botPacket = newBotPacket(i);
            ClientListServer.positionList.put(botNamesList.get(i), botPacket);
            ClientListServer.connectedIPs.add(botNamesList.get(i));
            botCircles.put(botNamesList.get(i),
                    new Circle(botPacket.posX + playerImage.getWidth() / 2 + 1,
                    botPacket.posY + playerImage.getHeight() / 2 + 1,
                    Math.max(playerImage.getWidth(), playerImage.getHeight()) / 2 + 1));
            alreadyLooking.put(botNamesList.get(i), false);
            soughtPositions.put(botNamesList.get(i), botAI.getNextPacket(botPacket, theBall.getPosition()));
        }
        //endregion

//        Wait until the AI is done loading the map
        while (!readyAI[0]) try { Thread.sleep(1); } catch (InterruptedException ignored) {}

        new Thread(this).start();
    }

    @Override
    public void run() {
        running = true;
        System.out.println("Multicasting on port: " + MULTICAST_PORT);
        System.out.println("Listening for clients...");

        //region Multicasts WelcomePackets
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
        //endregion

        //region Multicasts the positionList
        new Thread(() -> {
            long lastUpdate = System.nanoTime();
            double elapsedTime;
            while (running) {
                if (System.nanoTime() - lastUpdate < 1e9 / PACKETS_PER_SECOND)
                    continue;

                elapsedTime = (System.nanoTime() - lastUpdate) / 1e9;
                lastUpdate = System.nanoTime();

                gameFrame(elapsedTime);
            }
        }).start();
        //endregion

        //region Listens for incoming packets
        while (running) {
            if (ClientListServer.clientList.size() < startPositions.length) {
                try {
                    connectionListener();
                } catch (IOException e) { e.printStackTrace(); }
            }
        }
        //endregion

        multicastSocket.close();
    }

    private void gameFrame(double elapsedTime) {
        //region Move AI controlled players
        if (!ClientListServer.clientList.isEmpty() && ClientListServer.clientList.size() < startPositions.length)
            moveAI(elapsedTime);
        //endregion

        //region Ball's position and velocity if it is held
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
            theBall.resetVelocity();
            theBall.isHeld = true;
            //endregion

            if (!playerWithBall.bulletShot)
                continue;

            //region Ball's velocity if it's been shot
            Vector2 shotVel = new Vector2(ballX, ballY).multiply(ClientGame.SHOT_SPEED);
            theBall.setVelocity(new Vector2(shotVel).multiply(1.5));
            theBall.setDeceleration(new Vector2(shotVel).divide(100));
            theBall.isHeld = false;
            playerWithBall.bulletShot = false;
            playerWithBall.holdsBall = false;
            EventListenerServer.replaceEntry(ip, playerWithBall);
            //endregion

            break;
        }
        //endregion

        //region Ball collision
        if (!theBall.isHeld && previousBallVel.equals(theBall.getVelocity()))
            ballWallBounce();
        previousBallVel = theBall.getVelocity();
        //endregion

        //region Update the ball locally
        theBall.update(elapsedTime);
        //endregion

        //region Update ballPacket
        updateBallPacket();
        //endregion

        //region Sends the position list packet to all clients
        posListPacket.posList = ClientListServer.positionList;
        posListPacket.connectedIPs = ClientListServer.connectedIPs;
        multicastPacket(posListPacket, POSITIONS_PORT);
        //endregion

        //region Check if a goal has been scored
        if (theBall.getPosX() > mapWidth - GOAL_WIDTH || theBall.getPosX() < GOAL_WIDTH)
            resetMap();
        //endregion
    }

    private void updateBallPacket() {
        ballPacket.posX = theBall.getPosX();
        ballPacket.posY = theBall.getPosY();
        ballPacket.velX = theBall.getVelX();
        ballPacket.velY = theBall.getVelY();
        ballPacket.holdsBall = theBall.isHeld;
        EventListenerServer.replaceEntry("ball", ballPacket);
    }

    private void resetMap() {
        new Thread(() -> {
            System.out.println("Goal has been scored. Resetting positions...");
            theBall.reset();
            previousBallVel = theBall.getVelocity();
            updateBallPacket();

            for (String ip : ClientListServer.positionList.keySet()) {
                if (!botNamesList.contains(ip))
                    continue;

                soughtPositions.remove(ip);
                nextPositions.remove(ip);
                alreadyLooking.replace(ip, false);

                PositionPacket botPacket = ClientListServer.positionList.get(ip);
                botPacket.posX = botPacket.spawnX;
                botPacket.posY = botPacket.spawnY;
                botPacket.velX = botPacket.velY = 0;
                botPacket.bulletShot = false;
                botPacket.degrees = 0;
                botPacket.holdsBall = false;

                Circle botCircle = botCircles.get(ip);
                botCircle.setCenterX(botPacket.posX + playerImage.getWidth() / 2 + 1);
                botCircle.setCenterY(botPacket.posY + playerImage.getHeight() / 2 + 1);
                botCircles.replace(ip, botCircle);

                EventListenerServer.replaceEntry(ip, botPacket);
            }

            posListPacket.posList = ClientListServer.positionList;
            posListPacket.connectedIPs = ClientListServer.connectedIPs;
            multicastPacket(posListPacket, POSITIONS_PORT);
        }).start();

        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
    }

    private void moveAI(double time) {
        for (String ip : ClientListServer.connectedIPs) {
            if (!botNamesList.contains(ip))
                continue;

            PositionPacket botCurrentPos = ClientListServer.positionList.get(ip);
            PositionPacket botSoughtPos = soughtPositions.get(ip);
            Circle botCircle = botCircles.get(ip);

            //region Updates the bot's position locally
            botCurrentPos.posX += botCurrentPos.velX * time;
            botCurrentPos.posY += botCurrentPos.velY * time;
            botCircle.setCenterX(botCurrentPos.posX + playerImage.getWidth() / 2 + 1);
            botCircle.setCenterY(botCurrentPos.posY + playerImage.getHeight() / 2 + 1);
            botCircles.replace(ip, botCircle);
            //endregion

            //region Checks if bot is holding the ball
            botCurrentPos.holdsBall = theBall.getBounds().intersects(botCircle.getBoundsInLocal());
            //endregion

            //region Sets the currently sought after and next positions
            if (botSoughtPos == null) {
                botSoughtPos = botAI.getNextPacket(botCurrentPos, getNextGoal(botCurrentPos));
                soughtPositions.put(ip, botSoughtPos);
            } else if (Math.abs(botCurrentPos.posX - 2) < Math.abs(botSoughtPos.posX) &&
                    Math.abs(botCurrentPos.posX + 2) > Math.abs(botSoughtPos.posX) &&
                    Math.abs(botCurrentPos.posY - 2) < Math.abs(botSoughtPos.posY) &&
                    Math.abs(botCurrentPos.posY + 2) > Math.abs(botSoughtPos.posY)) {
                botCurrentPos.posX = botSoughtPos.posX;
                botCurrentPos.posY = botSoughtPos.posY;
                botSoughtPos = nextPositions.get(ip);
                soughtPositions.replace(ip, botSoughtPos);
                nextPositions.remove(ip);
            }
            //endregion

            //region If there is no next position, computes one on another Thread
            if (nextPositions.get(ip) == null && !alreadyLooking.get(ip)) {
                alreadyLooking.replace(ip, true);
                PositionPacket finalSeekedPos = botSoughtPos;
                new Thread (() -> {
                    nextPositions.put(ip,
                            botAI.getNextPacket(finalSeekedPos, getNextGoal(botCurrentPos)));
                    alreadyLooking.replace(ip, false);
                }).start();
            }
            //endregion

            //region Sets bot's rotation
            Vector2 botCenter = new Vector2(botCurrentPos.posX, botCurrentPos.posY);
            Vector2 direction = new Vector2(botSoughtPos.posX, botSoughtPos.posY).subtract(botCenter);
            botCurrentPos.degrees = (short) Math.toDegrees(Math.atan2(direction.getY(), direction.getX()));
            //endregion

            //region Changes bot's velocity for the next cycle
            if (botSoughtPos.posX > botCurrentPos.posX)         botCurrentPos.velX = 100;
            else if (botSoughtPos.posX < botCurrentPos.posX)    botCurrentPos.velX = -100;
            else                                                botCurrentPos.velX = 0;

            if (botSoughtPos.posY > botCurrentPos.posY)         botCurrentPos.velY = 100;
            else if (botSoughtPos.posY < botCurrentPos.posY)    botCurrentPos.velY = -100;
            else                                                botCurrentPos.velY = 0;
            //endregion

            EventListenerServer.replaceEntry(ip, botCurrentPos);
        }
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

            if ((rel_x < 0 && rel_y > 0 && rel_x + rel_y > 0) ||
                    (rel_x > 0 && rel_y > 0 && rel_y - rel_x > 0) ||
                    (rel_x < 0 && rel_y < 0 && rel_y - rel_x < 0) ||
                    (rel_x > 0 && rel_y < 0 && rel_y + rel_x < 0)) {
                theBall.collision(2);
            } else {
                theBall.collision(1);
            }
        }
    }

    private Vector2 getNextGoal(PositionPacket bot) {
        if (bot.holdsBall) {
            if (bot.spawnX < mapWidth / 2f) {
                return new Vector2(mapWidth - GOAL_WIDTH, mapHeight / 2f);
            } else {
                return new Vector2(GOAL_WIDTH, mapHeight / 2f);
            }
        }

        return theBall.getPosition();
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
        if (ClientListServer.clientList.size() >= startPositions.length) {
            System.out.println("Server is full");
            welcoming = false;
            return;
        }
        //endregion

        UniquePortPacket portPacket = new UniquePortPacket();
        //region Client's UniqueInfo and new positionList entry
        portPacket.setPort(clientPort);
        portPacket.setClientAddress(address);
        for (int i = 0; i < startPositions.length; i++) {
            Vector2 startPosition = startPositions[i];
            if (availablePositions.get(startPosition)) {
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
                availablePositions.replace(startPosition, false);
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
        if (ClientListServer.clientList.size() >= startPositions.length) {
            welcoming = true;
        }
        availablePositions.replace(ClientListServer.clientList.get(address).getSpawnPoint(), true);

        double clientSpawnX = ClientListServer.positionList.get(address.toString()).spawnX;
        double clientSpawnY = ClientListServer.positionList.get(address.toString()).spawnY;

        //Removes the player from positionList and adds a bot in its stead
        for (int i = 0; i < startPositions.length; i++) {
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
        pp.holdsBall = false;
        pp.bulletShot = false;
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
