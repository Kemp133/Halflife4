package com.halflife3.Networking.Server;

import com.halflife3.Controller.ObjectManager;
import com.halflife3.GameModes.MainMode;
import com.halflife3.GameObjects.*;
import com.halflife3.Mechanics.AI.AI;
import com.halflife3.Mechanics.Vector2;
import com.halflife3.Networking.NetworkingUtilities;
import com.halflife3.Networking.Packets.*;
import com.halflife3.View.MapRender;

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
	public static final int    SERVER_TIMEOUT    = 3000; // seconds
	public static final float  STUN_DURATION     = SERVER_FPS * 3;

	private        boolean                   running;
	private boolean                   welcoming;
	private InetAddress               multicastGroup;
	private MulticastSocket           multicastSocket;
	private        PositionListPacket        posListPacket;
	private        DatagramSocket            clientSocket;
	private        EventListenerServer       listenerServer;
	private int                       clientPort   = 6666;
	private HashMap<Vector2, Boolean> availablePositions;
	private HashMap<Vector2, Boolean> canShoot;
	public  Vector2[]                 startPositions;
	public  ArrayList<String>         botNamesList = new ArrayList<>(Arrays.asList("bot1", "bot2"));
	private HashMap<String, AIPlayer> botList;
	private        AI                        botAI;
	private        Ball                      theBall;
	private        Vector2                   previousBallVel;
	private        HashSet<Bullet>           bulletSet;
	private        ClientList                clientList;
	//endregion

	public void start() {
		//region Loads the map
		MapRender.LoadLevel();
		//endregion

		//region Object initialisation
		botAI              = new AI();
		previousBallVel    = new Vector2();
		botList            = new HashMap<>();
		bulletSet          = new HashSet<>();
		availablePositions = new HashMap<>();
		canShoot           = new HashMap<>();
		clientList         = new ClientList();
		posListPacket      = new PositionListPacket();
		listenerServer     = new EventListenerServer();
		startPositions     = MapRender.getStartPositions();
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

		//region Adds the ball to the positionList
		theBall = new Ball(MapRender.getBallSpawnPos());
		ObjectManager.removeObject(theBall);

		clientList.positionList.put("ball", theBall.getPositionPacket());
		//endregion

		//region Fills the positionList with bot players, giving them available starting positions
		for (int i = 0; i < startPositions.length; i++) {
			availablePositions.put(startPositions[i], true);
			canShoot.put(startPositions[i], true);
			String   botName   = botNamesList.get(i);
			AIPlayer botPlayer = new AIPlayer(new Vector2(startPositions[i]));
			ObjectManager.removeObject(botPlayer);
			botPlayer.setIpOfClient(botName);
			botPlayer.setActive(true);
			botPlayer.setSoughtPos(botAI.getNextPos(botPlayer.getPosition(), theBall.getPosition()));
			botList.put(botName, botPlayer);
			clientList.positionList.put(botName, botPlayer.getPacketToSend());
			clientList.connectedIPs.add(botName);
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
			int timeOut = SERVER_TIMEOUT;
			welcoming = true;
			while (running && timeOut > 0) {
				if (welcoming)
					multicastPacket(new WelcomePacket(), MULTICAST_PORT);

				NetworkingUtilities.WaitXSeconds(1);

				if (clientList.connectedList.isEmpty()) {
					if (timeOut <= 3)
						System.out.println("Timeout in: " + timeOut);
					timeOut--;
				} else
					timeOut = SERVER_TIMEOUT;
			}
			running   = false;
			welcoming = false;
			clientSocket.close();
		}).start();
		//endregion

		//region Tracks the state of the game
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

		//region Listens for incoming connections
		while (running) {
			if (clientList.connectedList.size() < startPositions.length) {
				try {
					connectionListener();
				} catch (IOException e) { e.printStackTrace(); }
			}
		}
		//endregion

		exit();
	}

	private void exit() {
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
		if (!clientList.connectedList.isEmpty() && clientList.connectedList.size() < startPositions.length)
			for (String ip : botList.keySet())
				moveAI(elapsedTime, ip);
		//endregion

		//region Bullets and the ball - position/velocity
		theBall.isHeld = false;
		for (String ip : clientList.connectedIPs) {
			PositionPacket player = clientList.positionList.get(ip);

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
				if (!canShoot.get(new Vector2(player.spawnX, player.spawnY)))
					continue;

				Vector2 shotVel = new Vector2(ballX, ballY).multiply(MainMode.SHOT_SPEED);

				if (player.holdsBall) {
					theBall.setVelocity(new Vector2(shotVel).multiply(1.5));
					theBall.setDeceleration(new Vector2(shotVel).divide(100));
					theBall.isHeld    = false;
					player.bulletShot = false;
					player.holdsBall  = false;
					if (botList.get(ip) != null) {
						botList.get(ip).setHoldsBall(false);
						botList.get(ip).setBulletShot(false);
					}
					listenerServer.replaceEntry(ip, player, clientList);
				} else {
					Vector2 bulletDir = new Vector2(ballX * 32, ballY * 32);
					Vector2 bulletPos = new Vector2(player.posX + 6, player.posY + 6).add(bulletDir);
					Bullet  bullet    = new Bullet(bulletPos, shotVel, "human");
					ObjectManager.removeObject(bullet);
					addBullet(bullet);
				}

				canShoot.replace(new Vector2(player.spawnX, player.spawnY), false);
			} else
				canShoot.replace(new Vector2(player.spawnX, player.spawnY), true);
		}
		//endregion

		for (Bullet b : getBulletSet()) { b.update(elapsedTime); } //Updates the bullet positions

		//region Updates the ball and its packet
		theBall.update(elapsedTime);
		listenerServer.replaceEntry("ball", theBall.getPositionPacket(), clientList);
		//endregion

		//region Sends the position list packet to all clients
		posListPacket.posList      = clientList.positionList;
		posListPacket.connectedIPs = clientList.connectedIPs;
		multicastPacket(posListPacket, POSITIONS_PORT);
		//endregion

		if (goalScored()) { resetMap(); }// Checks if a goal has been scored and resets the map if so

	}

	private boolean goalScored() {
		for (Goal g : MapRender.getGoalZone())
			if (theBall.getBounds().intersects(g.getBounds().getBoundsInLocal()))
				return true;

		return false;
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
			listenerServer.replaceEntry(name, bot.getPacketToSend(), clientList);
			return;
		}

//		  Checks if the bot is touching the ball
		if (!theBall.isHeld)
			bot.setHoldsBall(theBall.getBounds().intersects(bot.circle.getBoundsInLocal()));

//		  Checks if the bot has a position to move to or if it has reached it already
		if (bot.getSoughtPos() == null) {
			bot.setSoughtPos(botAI.getNextPos(bot.getPosition(), getNextGoal(bot)));
			if (bot.getSoughtPos() == null) {
				new Thread(() -> {
					System.err.println("Bot is lost. Stopping movement");
					while (bot.getSoughtPos() == null) {
						bot.stunned = SERVER_FPS / 2f;
						bot.setSoughtPos(botAI.getNextPos(bot.getPosition(), getNextGoal(bot)));
					}
				}).start();
				return;
			}
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

//		Shoots bullets at enemies and ball into the goal
		bot.setBulletShot(false);
		Vector2 enemyToShoot = getEnemyInRadius(bot);
		Goal    goal         = goalInRadius(bot);
		if (enemyToShoot != null && !bot.isHoldingBall()) {// Faces an enemy
			Vector2 toEnemy = enemyToShoot.subtract(bot.getPosition());
			bot.setDegrees((short) Math.toDegrees(Math.atan2(toEnemy.getY(), toEnemy.getX())));
			if (bot.reload == bot.RELOAD_DURATION && bot.stunned == 0) {
				bot.setBulletShot(true);
				bot.reload = 0;
			}
		} else if (bot.isHoldingBall() && goal != null) {//Faces the goal
			Vector2 toGoal = goal.getPosition().subtract(bot.getPosition());
			bot.setDegrees((short) Math.toDegrees(Math.atan2(toGoal.getY(), toGoal.getX())));
			if (bot.reload == bot.RELOAD_DURATION && bot.stunned == 0) {
				bot.setBulletShot(true);
				bot.reload  = 0;
				bot.stunned = 100;
			}
		} else {// Faces the way it's going
			Vector2 direction = new Vector2(bot.getSoughtPos()).subtract(bot.getPosition());
			bot.setDegrees((short) Math.toDegrees(Math.atan2(direction.getY(), direction.getX())));
		}

		//region Sets the bot's velocity on the X axis
		if (bot.getSoughtPos().getX() > bot.getPosX())
			bot.setVelocity(MainMode.MOVEMENT_SPEED, bot.getVelY());
		else if (bot.getSoughtPos().getX() < bot.getPosX())
			bot.setVelocity(-MainMode.MOVEMENT_SPEED, bot.getVelY());
		else
			bot.setVelocity(0, bot.getVelY());
		//endregion

		//region Sets the bot's velocity on the Y axis
		if (bot.getSoughtPos().getY() > bot.getPosY())
			bot.setVelocity(bot.getVelX(), MainMode.MOVEMENT_SPEED);
		else if (bot.getSoughtPos().getY() < bot.getPosY())
			bot.setVelocity(bot.getVelX(), -MainMode.MOVEMENT_SPEED);
		else
			bot.setVelocity(bot.getVelX(), 0);
		//endregion

//		  Replaces the position packet of the bot
		listenerServer.replaceEntry(name, bot.getPacketToSend(), clientList);
	}

	private Goal goalInRadius(AIPlayer bot) {
		for (Goal g : MapRender.getGoalZone())
			if (bot.getPosition().distance(g.getPosition()) < 200)
				return g;
		return null;
	}

	private Vector2 getEnemyInRadius(AIPlayer bot) {
		for (var ip : clientList.connectedIPs) {
			if (ip.equals(bot.getIpOfClient()))
				continue;
			PositionPacket enemy   = clientList.positionList.get(ip);
			Vector2        toEnemy = new Vector2(enemy.posX, enemy.posY);
			if (bot.getPosition().distance(toEnemy) < 250)
				return toEnemy;
		}

		return null;
	}

	private boolean arrived(Vector2 current, Vector2 sought) {
		return (Math.abs(current.getX() - 2) < Math.abs(sought.getX()) &&
		        Math.abs(current.getX() + 2) > Math.abs(sought.getX()) &&
		        Math.abs(current.getY() - 2) < Math.abs(sought.getY()) &&
		        Math.abs(current.getY() + 2) > Math.abs(sought.getY()));
	}

	private Vector2 getNextGoal(AIPlayer bot) {
		if (bot.isHoldingBall()) {
			if (bot.getSpawnPosition().getX() < MapRender.mapWidth / 2f) {
				for (Goal g : MapRender.getGoalZone())
					if (g.getScoringTeam() == 'L') { return g.getPosition(); }
			} else {
				for (Goal g : MapRender.getGoalZone())
					if (g.getScoringTeam() == 'R') { return g.getPosition(); }
			}
		}

		return theBall.getPosition();
	}

	private void ballWallBounce() {
		for (Brick block : MapRender.GetList()) {
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
		for (Bullet bullet : getBulletSet()) {
			for (Brick block : MapRender.GetList())
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
			removeBullet(bullet);
	}

	private void connectionListener() throws IOException {
		byte[]         pokeBuf = new byte[NetworkingUtilities.objectToByteArray(new ConnectPacket()).length];
		DatagramPacket incPoke = new DatagramPacket(pokeBuf, pokeBuf.length);

		if (clientList.connectedList.isEmpty()) {
			clientSocket.setSoTimeout(SERVER_TIMEOUT * 1001);
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
		listenerServer.received(receivedPoke, incPoke.getAddress(), this, clientList);
	}

	public synchronized void addConnection(InetAddress address) {
		//region Checks if Server is full
		if (clientList.connectedList.size() >= startPositions.length) {
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
			Vector2 startPosition = new Vector2(startPositions[i]);
			if (!availablePositions.get(startPosition))
				continue;

//            Sets the start position for the UniqueInfo packet
			portPacket.setStartPosition(startPosition);

//            Removes the bot holding the [i]th startPosition
			String botName = botNamesList.get(i);
			clientList.positionList.remove(botName);
			clientList.connectedIPs.remove(botName);
			botList.get(botName).setActive(false);

//			  Adds the player (with the [i]th startPosition) to the positionList
			PositionPacket playerPacket = new PositionPacket();
			playerPacket.posX = playerPacket.spawnX = startPosition.getX();
			playerPacket.posY = playerPacket.spawnY = startPosition.getY();
			clientList.positionList.put(address.toString(), playerPacket);
			clientList.connectedIPs.add(address.toString());

//            Disables the [i]th startPosition so that no new players could have it assigned to them
			availablePositions.replace(startPosition, false);
			break;
		}
		//endregion

		//Resets the position of every bot
		for (AIPlayer bot : botList.values())
			bot.resetBasics();

		ConnectedToServer connection = new ConnectedToServer(address, clientPort, portPacket.getStartPosition(), this,
				clientList);
		new Thread(connection).start();

		//Lets the client side get ready to receive the port
		NetworkingUtilities.WaitXSeconds(3);
		multicastPacket(portPacket, GET_PORT_PORT);

		clientList.connectedList.put(address, connection);

		clientPort++;
		welcoming = true;
	}

	/**
	 * Removes the player from positionList and adds a bot in its stead
	 *
	 * @param address The address of the player to be removed
	 */
	public void removeConnection(InetAddress address) {
		if (clientList.connectedList.size() == 1) {
			running = false;
			return;
		}

		if (clientList.connectedList.size() >= startPositions.length)
			welcoming = true;

		Vector2 spawnPoint = clientList.connectedList.get(address).getSpawnPoint();

		availablePositions.replace(spawnPoint, true);

		double clientSpawnX = spawnPoint.getX();
		double clientSpawnY = spawnPoint.getY();

		for (int i = 0; i < startPositions.length; i++) {
			if (clientSpawnX != startPositions[i].getX() || clientSpawnY != startPositions[i].getY())
				continue;

			String botName = botNamesList.get(i);

			clientList.positionList.remove(address.toString());
			clientList.positionList.put(botName, botList.get(botName).getPacketToSend());

			clientList.connectedIPs.remove(address.toString());
			clientList.connectedIPs.add(botName);
		}

		clientList.connectedList.get(address).close();
		clientList.connectedList.remove(address);
		System.out.println(address + " has disconnected");
	}

	public synchronized void multicastPacket(Object o, int mPort) {
		try {
			byte[] sendBuf = NetworkingUtilities.objectToByteArray(o);
			var    packet  = new DatagramPacket(sendBuf, sendBuf.length, multicastGroup, mPort);
			multicastSocket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void resetMap() {
		new Thread(() -> {
			System.out.println("Goal has been scored. Resetting positions...");

			theBall.reset();
			previousBallVel = new Vector2();
			listenerServer.replaceEntry("ball", theBall.getPositionPacket(), clientList);

			for (String ip : botList.keySet()) {
				botList.get(ip).reset();
				listenerServer.replaceEntry(ip, botList.get(ip).getPacketToSend(), clientList);
			}
		}).start();

		NetworkingUtilities.WaitXSeconds(1);
		posListPacket.connectedIPs = clientList.connectedIPs;
		posListPacket.posList      = clientList.positionList;
		multicastPacket(posListPacket, POSITIONS_PORT);
	}

	private synchronized void addBullet(Bullet toAdd) {
		bulletSet.add(toAdd);
	}

	private synchronized void removeBullet(Bullet toRemove) {
		bulletSet.remove(toRemove);
	}

	private synchronized HashSet<Bullet> getBulletSet() {
		return new HashSet<>(bulletSet);
	}
}
