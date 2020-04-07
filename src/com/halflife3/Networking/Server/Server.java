package com.halflife3.Networking.Server;

import com.halflife3.Controller.ObjectManager;
import com.halflife3.GameModes.MainMode;
import com.halflife3.GameObjects.AIPlayer;
import com.halflife3.GameObjects.Ball;
import com.halflife3.GameObjects.Bricks;
import com.halflife3.GameObjects.Bullet;
import com.halflife3.GameUI.Maps;
import com.halflife3.Mechanics.AI.AI;
import com.halflife3.Mechanics.Vector2;
import com.halflife3.Networking.NetworkingUtilities;
import com.halflife3.Networking.Packets.*;
import com.halflife3.View.MapRender;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class Server implements Runnable {

	public static final int SERVER_FPS = 90;

	//region Variables
	public static final String MULTICAST_ADDRESS = "239.255.42.99";
	public static final int    MULTICAST_PORT    = 5555;
	public static final int    LISTENER_PORT     = 5544;
	public static final int    GET_PORT_PORT     = 5566;
	public static final int    POSITIONS_PORT    = 5533;
	public static final int    GOAL_WIDTH        = 4 * 40;
	public static final int    SERVER_TIMEOUT    = 3000000; // milliseconds
	public static final float  STUN_DURATION     = SERVER_FPS * 3;

	private        boolean                   running        = false;
	private static boolean                   welcoming      = true;
	private static InetAddress               multicastGroup;
	private static MulticastSocket           multicastSocket;
	private        PositionListPacket        posListPacket;
	private        DatagramSocket            clientSocket;
	private        EventListenerServer       listenerServer;
	private static int                       clientPort     = 6666;
	private static HashMap<Vector2, Boolean> availablePositions;
	private static HashMap<Vector2, Boolean> canShoot;
	public static  Vector2[]                 startPositions = {new Vector2(/*160*/600, 600), new Vector2(1840, 600)};
	public static  ArrayList<String>         botNamesList   = new ArrayList<>(Arrays.asList("bot1", "bot2"));
	private static HashMap<String, AIPlayer> botList;
	private        AI                        botAI;
	private        Ball                      theBall;
	private        int                       mapWidth;
	private        int                       mapHeight;
	private        Vector2                   previousBallVel;
	private        HashSet<Bullet>           bulletSet;
	//endregion

	public void start() {
		//region Object initialisation
		botAI              = new AI();
		previousBallVel    = new Vector2();
		botList            = new HashMap<>();
		bulletSet          = new HashSet<>();
		availablePositions = new HashMap<>();
		canShoot           = new HashMap<>();
		posListPacket      = new PositionListPacket();
		listenerServer     = new EventListenerServer();
		//endregion

		final boolean[] readyAI = {false};
		new Thread(() -> readyAI[0] = botAI.setupMap()).start();

		//region Sets up the communication sockets
		try {
			clientSocket    = new DatagramSocket(LISTENER_PORT);
			multicastGroup  = InetAddress.getByName(MULTICAST_ADDRESS);
			multicastSocket = new MulticastSocket();
			multicastSocket.setInterface(NetworkingUtilities.setWifiInterface());
		} catch (SocketException e) {
			NetworkingUtilities.CreateErrorMessage("Error Setting Network Interface",
					"Network interface could not be " + "set", e.getMessage());
		} catch (IOException e) { e.printStackTrace(); }
		//endregion

		//region Loads the map
		MapRender.LoadLevel();
		//endregion

		//region Adds the ball to the positionList
		try {
			BufferedImage mapImage        = ImageIO.read(new File(Maps.Map));
			int           mapWidthMiddle  = mapImage.getWidth() * 20;
			int           mapHeightMiddle = mapImage.getHeight() * 20;
			mapWidth  = mapWidthMiddle * 2;
			mapHeight = mapHeightMiddle * 2;

			theBall = new Ball(new Vector2(mapWidthMiddle, mapHeightMiddle));
			ObjectManager.removeObject(theBall);

			ClientListServer.positionList.put("ball", theBall.getPositionPacket());
		} catch (IOException e) { e.printStackTrace(); }
		//endregion

		//region Fills the positionList with bot players, giving them available starting positions
		for (int i = 0; i < startPositions.length; i++) {
			availablePositions.put(startPositions[i], true);
			canShoot.put(startPositions[i], true);
			String   botName   = botNamesList.get(i);
			AIPlayer botPlayer = new AIPlayer(startPositions[i]);
			ObjectManager.removeObject(botPlayer);
			botPlayer.setIpOfClient(botName);
			botPlayer.setActive(true);
			botPlayer.setSoughtPos(botAI.getNextPos(botPlayer.getPosition(), theBall.getPosition()));
			botList.put(botName, botPlayer);
			ClientListServer.positionList.put(botName, botPlayer.getPacketToSend());
			ClientListServer.connectedIPs.add(botName);
		}
		//endregion

//		Wait until the AI is done loading the map
		while (!readyAI[0])
			try { Thread.sleep(1); } catch (InterruptedException ignored) {}

		new Thread(this).start();
	}

	@Override
	public void run() {
		running = true;
		System.out.println("Multicasting on port: " + MULTICAST_PORT);
		System.out.println("Listening for clients...");

		//region Multicasts WelcomePackets
		new Thread(() -> {
			int timeOut = SERVER_TIMEOUT / 1000;
			while (running && timeOut > 0) {
				if (welcoming) { multicastPacket(new WelcomePacket(), MULTICAST_PORT); }

				waitASecond();

				if (ClientListServer.clientList.isEmpty()) {
					if (timeOut <= 3)
						System.out.println("Timeout in: " + timeOut);
					timeOut--;
				} else
					timeOut = SERVER_TIMEOUT / 1000;
			}
			running   = false;
			welcoming = false;
			clientSocket.close();
		}).start();
		//endregion

		//region Multicasts the positionList
		new Thread(() -> {
			long   lastUpdate = System.nanoTime();
			double elapsedTime;
			while (running) {
				if (System.nanoTime() - lastUpdate < 1e9 / SERVER_FPS)
					continue;

				elapsedTime = (System.nanoTime() - lastUpdate) / 1e9;
				lastUpdate  = System.nanoTime();

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
		bulletCollision(); //Destroy bullets if they hit something

		//region Ball collision
		if (!theBall.isHeld && !previousBallVel.equals(theBall.getVelocity()))
			ballWallBounce();
		previousBallVel = theBall.getVelocity();
		//endregion

		//region Updates AI controlled players
		if (!ClientListServer.clientList.isEmpty() && ClientListServer.clientList.size() < startPositions.length)
			for (String ip : botList.keySet())
				moveAI(elapsedTime, ip);
		//endregion

		//region Bullets and the ball - position/velocity
		for (String ip : ClientListServer.positionList.keySet()) {
			if (ip.equals("ball"))
				continue;

			PositionPacket player = ClientListServer.positionList.get(ip);

			double degreeRadians = Math.toRadians(player.degrees);
			double ballX         = Math.cos(degreeRadians);
			double ballY         = Math.sin(degreeRadians);

			if (player.holdsBall) {
				Vector2 ballDir = new Vector2(ballX * 35, ballY * 35);
				Vector2 ballPos = new Vector2(player.posX + 6, player.posY + 6).add(ballDir);
				theBall.setPosition(ballPos);
				theBall.resetVelocity();
				theBall.isHeld = true;
			}

			if (player.bulletShot) {
				if (canShoot.get(new Vector2(player.spawnX, player.spawnY))) {
					Vector2 shotVel = new Vector2(ballX, ballY).multiply(MainMode.SHOT_SPEED);

					if (player.holdsBall) {
						theBall.setVelocity(new Vector2(shotVel).multiply(1.5));
						theBall.setDeceleration(new Vector2(shotVel).divide(100));
						theBall.isHeld    = false;
						player.bulletShot = false;
						player.holdsBall  = false;
						EventListenerServer.replaceEntry(ip, player);
					} else {
						Vector2 bulletDir = new Vector2(ballX * 32, ballY * 32);
						Vector2 bulletPos = new Vector2(player.posX + 6, player.posY + 6).add(bulletDir);
						Bullet  bullet    = new Bullet(bulletPos, shotVel, "human");
						ObjectManager.removeObject(bullet);
						bulletSet.add(bullet);
					}

					canShoot.replace(new Vector2(player.spawnX, player.spawnY), false);
				}
			} else
				canShoot.replace(new Vector2(player.spawnX, player.spawnY), true);
		}
		//endregion

		for (var b : bulletSet)
			b.update(elapsedTime); //Updates the bullet positions

		//region Updates the ball and its packet
		theBall.update(elapsedTime);
		EventListenerServer.replaceEntry("ball", theBall.getPositionPacket());
		//endregion

		//region Sends the position list packet to all clients
		posListPacket.posList      = ClientListServer.positionList;
		posListPacket.connectedIPs = ClientListServer.connectedIPs;
		multicastPacket(posListPacket, POSITIONS_PORT);
		//endregion

		//region Check if a goal has been scored
		if (theBall.getPosX() > mapWidth - GOAL_WIDTH || theBall.getPosX() < GOAL_WIDTH)
			resetMap();
		//endregion
	}

	/**
	 * A method to update the position, velocity and rotation of the bot if it is active, check if the bot is holding
	 * the ball and find a path for the bot to follow using the A* algorithm
	 *
	 * @param time The time passed since the last frame update
	 * @param name The name of the bot to be updated
	 */
	private void moveAI(double time, String name) {
		AIPlayer bot = botList.get(name);

//	      If a bot is inactive - don't move it
		if (bot.inactive())
			return;

//		  Updates the bot's position
		bot.update(time);

		if (bot.stunned > 0) {
			EventListenerServer.replaceEntry(name, bot.getPacketToSend());
			return;
		}

//		  Checks if the bot is touching the ball
		if (!theBall.isHeld)
			bot.setHoldsBall(theBall.getBounds().intersects(bot.circle.getBoundsInLocal()));

//		  Checks if the bot has a position to move to or if it has reached it already
		if (bot.getSoughtPos() == null) {
			bot.setSoughtPos(botAI.getNextPos(bot.getPosition(), getNextGoal(bot)));
		} else if (arrived(bot.getPosition(), bot.getSoughtPos())) {
			bot.setPosition(bot.getSoughtPos());
			bot.setSoughtPos(bot.getNextPos());
			bot.setNextPos(null);
		}

//		  Calculates the next place to move to in the background
		if (bot.getNextPos() == null && !bot.isAlreadyLooking()) {
			bot.setAlreadyLooking(bot.getNextPos() == null);
			new Thread(() -> {
				bot.setNextPos(botAI.getNextPos(bot.getSoughtPos(), getNextGoal(bot)));
				bot.setAlreadyLooking(bot.getNextPos() == null);
			}).start();
		}

//		  Makes the bot face the way it's going
		Vector2 direction = new Vector2(bot.getSoughtPos()).subtract(bot.getPosition());
		bot.setDegrees((short) Math.toDegrees(Math.atan2(direction.getY(), direction.getX())));

		//region Sets the bot's velocity on the X axis
		if (bot.getSoughtPos().getX() > bot.getPosX())
			bot.setVelocity(100, bot.getVelY());
		else if (bot.getSoughtPos().getX() < bot.getPosX())
			bot.setVelocity(-100, bot.getVelY());
		else
			bot.setVelocity(0, bot.getVelY());
		//endregion

		//region Sets the bot's velocity on the Y axis
		if (bot.getSoughtPos().getY() > bot.getPosY())
			bot.setVelocity(bot.getVelX(), 100);
		else if (bot.getSoughtPos().getY() < bot.getPosY())
			bot.setVelocity(bot.getVelX(), -100);
		else
			bot.setVelocity(bot.getVelX(), 0);
		//endregion

//		  Replaces the position packet of the bot
		EventListenerServer.replaceEntry(name, bot.getPacketToSend());
	}

	private boolean arrived(Vector2 current, Vector2 sought) {
		return (Math.abs(current.getX() - 2) < Math.abs(sought.getX()) &&
		        Math.abs(current.getX() + 2) > Math.abs(sought.getX()) &&
		        Math.abs(current.getY() - 2) < Math.abs(sought.getY()) &&
		        Math.abs(current.getY() + 2) > Math.abs(sought.getY()));
	}

	private Vector2 getNextGoal(AIPlayer bot) {
		if (bot.isHoldingBall()) {
			if (bot.getSpawnPosition().getX() < mapWidth / 2f) {
				return new Vector2(mapWidth - GOAL_WIDTH, mapHeight / 2f);
			} else {
				return new Vector2(GOAL_WIDTH, mapHeight / 2f);
			}
		}

		return theBall.getPosition();
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
			double  rel_x       = relevantPos.getX();
			double  rel_y       = relevantPos.getY();

			if ((rel_x < 0 && rel_y > 0 && rel_x + rel_y > 0) || (rel_x > 0 && rel_y > 0 && rel_y - rel_x > 0) ||
			    (rel_x < 0 && rel_y < 0 && rel_y - rel_x < 0) || (rel_x > 0 && rel_y < 0 && rel_y + rel_x < 0)) {
				theBall.collision(2);
			} else {
				theBall.collision(1);
			}
		}
	}

	private void bulletCollision() {
		HashSet<Bullet> bulletsToDestroy = new HashSet<>();
		for (Bullet bullet : bulletSet) {
			for (Bricks block : MapRender.GetList())
				if (bullet.getBounds().intersects(block.getBounds().getBoundsInLocal()))
					bulletsToDestroy.add(bullet);

			for (AIPlayer bot : botList.values()) {
				if (bot.inactive())
					continue;

				if (!bullet.getBounds().intersects(bot.circle.getBoundsInLocal()))
					continue;

				bulletsToDestroy.add(bullet);

				if (bot.stunned != 0)
					continue;

				bot.stunned = STUN_DURATION;
				bot.setVelocity(bullet.getVelocity().divide(STUN_DURATION / SERVER_FPS));
				bot.setDeceleration(bot.getVelocity().divide(SERVER_FPS / 2f));
				bot.setHoldsBall(false);
				theBall.resetVelocity();
				theBall.setDeceleration(new Vector2());
				theBall.isHeld = false;
			}
		}

		for (Bullet bullet : bulletsToDestroy)
			bulletSet.remove(bullet);
	}

	private void connectionListener() throws IOException {
		byte[]         pokeBuf = new byte[NetworkingUtilities.objectToByteArray(new ConnectPacket()).length];
		DatagramPacket incPoke = new DatagramPacket(pokeBuf, pokeBuf.length);

		if (ClientListServer.clientList.isEmpty()) {
			clientSocket.setSoTimeout(SERVER_TIMEOUT + 1000);
		} else
			clientSocket.setSoTimeout(0);

		try { clientSocket.receive(incPoke); } catch (SocketTimeoutException e) {
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

	public synchronized static void addConnection(InetAddress address) {
		//region Checks if Server is full
		if (ClientListServer.clientList.size() >= startPositions.length) {
			System.out.println("Server is full. Player " + address + " disconnected");
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
			if (!availablePositions.get(startPosition))
				continue;

//            Sets the start position for the UniqueInfo packet
			portPacket.setStartPosition(startPosition);

//            Removes the bot holding the [i]th startPosition
			String botName = botNamesList.get(i);
			ClientListServer.positionList.remove(botName);
			ClientListServer.connectedIPs.remove(botName);
			botList.get(botName).setActive(false);

//			  Adds the player (with the [i]th startPosition) to the positionList
			PositionPacket playerPacket = new PositionPacket();
			playerPacket.posX = playerPacket.spawnX = startPosition.getX();
			playerPacket.posY = playerPacket.spawnY = startPosition.getY();
			ClientListServer.positionList.put(address.toString(), playerPacket);
			ClientListServer.connectedIPs.add(address.toString());

//            Disables the [i]th startPosition so that no new players could have it assigned to them
			availablePositions.replace(startPosition, false);
			break;
		}
		//endregion

		//Resets the position of every bot
		for (AIPlayer bot : botList.values())
			bot.resetBasics();

		ConnectedToServer connection = new ConnectedToServer(address, clientPort, portPacket.getStartPosition());
		new Thread(connection).start();

		//Lets the client side get ready to receive the port
		try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
		multicastPacket(portPacket, GET_PORT_PORT);

		ClientListServer.clientList.put(address, connection);

		clientPort++;
		welcoming = true;
	}

	/**
	 * Removes the player from positionList and adds a bot in its stead
	 *
	 * @param address The address of the player to be removed
	 */
	public static void removeConnection(InetAddress address) {
		if (ClientListServer.clientList.size() >= startPositions.length)
			welcoming = true;

		Vector2 spawnPoint = ClientListServer.clientList.get(address).getSpawnPoint();

		availablePositions.replace(spawnPoint, true);

		double clientSpawnX = spawnPoint.getX();
		double clientSpawnY = spawnPoint.getY();

		for (int i = 0; i < startPositions.length; i++) {
			if (clientSpawnX != startPositions[i].getX() || clientSpawnY != startPositions[i].getY())
				continue;

			String botName = botNamesList.get(i);

			ClientListServer.positionList.remove(address.toString());
			ClientListServer.positionList.put(botName, botList.get(botName).getPacketToSend());

			ClientListServer.connectedIPs.remove(address.toString());
			ClientListServer.connectedIPs.add(botName);
		}

		ClientListServer.clientList.get(address).close();
		ClientListServer.clientList.remove(address);
		System.out.println(address + " has disconnected");
	}

	public synchronized static void multicastPacket(Object o, int mPort) {
		try {
			byte[] sendBuf = NetworkingUtilities.objectToByteArray(o);
			var    packet  = new DatagramPacket(sendBuf, sendBuf.length, multicastGroup, mPort);
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

	private void resetMap() {
		new Thread(() -> {
			System.out.println("Goal has been scored. Resetting positions...");
			theBall.reset();
			previousBallVel = new Vector2();
			EventListenerServer.replaceEntry("ball", theBall.getPositionPacket());

			for (String ip : botList.keySet()) {
				botList.get(ip).reset();
				EventListenerServer.replaceEntry(ip, botList.get(ip).getPacketToSend());
			}

			posListPacket.connectedIPs = ClientListServer.connectedIPs;
			posListPacket.posList      = ClientListServer.positionList;
			multicastPacket(posListPacket, POSITIONS_PORT);
		}).start();

		try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
	}
}
