package com.halflife3.Networking.Server;

import com.halflife3.Controller.ObjectManager;
import com.halflife3.GameModes.MainMode;
import com.halflife3.GameObjects.*;
import com.halflife3.Networking.Server.AI.AI;
import com.halflife3.GameObjects.Vector2;
import com.halflife3.Networking.NetworkingUtilities;
import com.halflife3.Networking.Packets.*;
import com.halflife3.View.MapRender;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {

	public static final int SERVER_FPS = 90;

	//region Variables
	public static final String MULTICAST_ADDRESS = "239.255.42.99";
	public static final int    MULTICAST_PORT    = 5555;
	public static final int    LISTENER_PORT     = 5544;
	public static final int    GET_PORT_PORT     = 5566;
	public static final int    POSITIONS_PORT    = 5533;
	public final        float  STUN_DURATION     = SERVER_FPS * 3;

	private boolean                   running;
	private boolean                   welcoming;
	private boolean                   resetting;
	private InetAddress               multicastGroup;
	private MulticastSocket           multicastSocket;
	private PositionListPacket        posListPacket;
	private DatagramSocket            clientSocket;
	private EventListenerServer       listenerServer;
	private int                       clientPort;
	public  Vector2[]                 startPositions;
	private HashMap<Vector2, Boolean> availablePositions;
	private HashMap<Vector2, Boolean> canShoot;
	public  ArrayList<String>         botNamesList;
	private HashMap<String, AIPlayer> botList;
	private AI                        botAI;
	private Ball                      theBall;
	private Vector2                   previousBallVel;
	private HashSet<Bullet>           bulletSet;
	private ClientList                clientList;
	private ExecutorService           executor;
	//endregion

	/**
	 * Loads the map for the server to use, initialises all objects, including the <code>Ball</code> and the
	 * <code>AIPlayers</code>, sets the map up for the <code>AI</code> class to use in path-finding and starts an
	 * instance of this class, which implements the <code>Runnable</code> interface
	 */
	public void start() {
		//region Loads the map
		MapRender.LoadLevel();
		//endregion

		//region Object initialisation
		clientPort         = 6666;
		botAI              = new AI();
		previousBallVel    = new Vector2();
		botList            = new HashMap<>();
		bulletSet          = new HashSet<>();
		availablePositions = new HashMap<>();
		canShoot           = new HashMap<>();
		clientList         = new ClientList();
		botNamesList       = new ArrayList<>();
		posListPacket      = new PositionListPacket();
		listenerServer     = new EventListenerServer();
		startPositions     = MapRender.getStartPositions();
		executor           = Executors.newFixedThreadPool(2);

		for (int i = 1; i <= startPositions.length; i++)
		     botNamesList.add(String.format("bot%d", i));
		//endregion

		final boolean[] readyAI = {false};
		new Thread(() -> readyAI[0] = botAI.setupMap()).start();

		//region Sets up the communication sockets
		try {
			clientSocket    = new DatagramSocket(LISTENER_PORT);
			multicastGroup  = InetAddress.getByName(MULTICAST_ADDRESS);
			multicastSocket = new MulticastSocket();
			multicastSocket.setInterface(NetworkingUtilities.getWifiInterface());
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
		while (!readyAI[0]) {try { Thread.sleep(1); } catch (InterruptedException ignored) {}}

		new Thread(this).start();
	}

	@Override
	public void run() {
		running   = true;
		welcoming = true;
		resetting = false;
		log("Multicasting on port: " + MULTICAST_PORT + '\n' + "Listening for clients...");

		//region Multicasts WelcomePackets
		executor.submit(() -> {
			while (running) {
				if (welcoming) {
					multicastPacket(new WelcomePacket(), MULTICAST_PORT);
					NetworkingUtilities.WaitXSeconds(1);
				}
			}
		});
		//endregion

		//region Tracks the state of the game
		executor.submit(() -> {
			long   lastUpdate = System.nanoTime();
			double elapsedTime;
			while (running) {
				if (System.nanoTime() - lastUpdate < 1e9 / SERVER_FPS)
					continue;

				elapsedTime = (System.nanoTime() - lastUpdate) / 1e9;
				lastUpdate  = System.nanoTime();

				if (resetting) { continue; }

				gameFrame(elapsedTime);
			}
		});
		//endregion

		//region Listens for incoming connections
		while (running)
			if (clientList.connectedList.size() < startPositions.length)
				connectionListener();
		//endregion
	}

	/**
	 * Stops all <code>Threads</code> and closes all communication sockets, stopping all processes running in this
	 * class instance
	 */
	private void exit() {
		running   = false;
		welcoming = false;
		executor.shutdownNow();
		clientSocket.close();
		multicastSocket.close();
		log("Server closed");
	}

	/**
	 * Constitutes one frame in the game. Calls upon different methods to detect collisions, update object positions,
	 * create and destroy <code>Bullets</code>, multicast updated object positions and check if a goal has been scored
	 *
	 * @param elapsedTime The time that has passed since the last time the method was called
	 */
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

	//region Running game methods
	/**
	 * Iterates through all the <code>Goal</code> nodes and checks if any of them intersect with the <code>Ball</code>
	 *
	 * @return <code>true</code> if and only if any <code>Goal</code> object intersects with the <code>Ball</code>
	 * object, otherwise returns <code>false</code>
	 */
	private boolean goalScored() {
		for (Goal g : MapRender.getGoalZone())
			if (theBall.getBounds().intersects(g.getBounds().getBoundsInLocal()))
				return true;

		return false;
	}

	/**
	 * Checks if the <code>Ball</code> intersects any <code>Brick</code> objects and if so adjusts the
	 * <code>Ball's</code> velocity accordingly
	 */
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

	/**
	 * Iterates through all the <code>Bullet</code> objects in the server's <code>bulletList</code> and destroys them
	 * if they intersect with either a <code>Brick</code> or an <code>AIPlayer</code> object. In the latter case, the
	 * <code>AIPlayer</code> is stunned and the <code>Ball</code> is dropped.
	 */
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

	/**
	 * Calls the reset() method of the "<code>Ball</code>, and all the <code>AIPlayer</code> objects and then
	 * multicasts the newly reset positions to all connected clients
	 */
	private void resetMap() {
		resetting = true;

		new Thread(() -> {
			log("Goal has been scored. Resetting positions...");

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

		new Thread(() -> {
			NetworkingUtilities.WaitXSeconds(1);
			resetting = false;
		}).start();
	}
	//endregion

	/**
	 * Updates the position, velocity and rotation of the bot if it is active, checks if the bot is holding
	 * the ball and finds a path for the bot to follow using the A* algorithm
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
			if (bot.reload == bot.RELOAD_TIME && bot.stunned == 0) {
				bot.setBulletShot(true);
				bot.reload = 0;
			}
		} else if (bot.isHoldingBall() && goal != null) {//Faces the goal
			Vector2 toGoal = goal.getPosition().subtract(bot.getPosition());
			bot.setDegrees((short) Math.toDegrees(Math.atan2(toGoal.getY(), toGoal.getX())));
			if (bot.reload == bot.RELOAD_TIME && bot.stunned == 0) {
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
			bot.setVelocity(MainMode.NORMAL_SPEED, bot.getVelY());
		else if (bot.getSoughtPos().getX() < bot.getPosX())
			bot.setVelocity(-MainMode.NORMAL_SPEED, bot.getVelY());
		else
			bot.setVelocity(0, bot.getVelY());
		//endregion

		//region Sets the bot's velocity on the Y axis
		if (bot.getSoughtPos().getY() > bot.getPosY())
			bot.setVelocity(bot.getVelX(), MainMode.NORMAL_SPEED);
		else if (bot.getSoughtPos().getY() < bot.getPosY())
			bot.setVelocity(bot.getVelX(), -MainMode.NORMAL_SPEED);
		else
			bot.setVelocity(bot.getVelX(), 0);
		//endregion

//		  Replaces the position packet of the bot
		listenerServer.replaceEntry(name, bot.getPacketToSend(), clientList);
	}

	//region AI helper methods
	/**
	 * Checks if there is a <code>Goal</code> node in a 200 pixel radius around the bot which is passed as an argument
	 *
	 * @param bot The <code>AIPlayer</code> object to get the distance to from the <code>Goal</code> nodes
	 * @return <code>Goal</code> object if and only if such a node exists, that the distance between it and the
	 * <code>AIPlayer</code> is less than 200 pixels, otherwise returns <code>null</code>
	 */
	private Goal goalInRadius(AIPlayer bot) {
		for (Goal g : MapRender.getGoalZone())
			if (bot.getPosition().distance(g.getPosition()) < 200)
				return g;

		return null;
	}

	/**
	 * Checks if there is an enemy <code>PositionPacket</code> in a 250 pixel radius around the bot which is passed as
	 * an argument
	 *
	 * @param bot The <code>AIPlayer</code> object to get the distance to from the <code>PositionPacket</code>
	 *               objects saved in the <code>positionList</code> HashMap
	 * @return <code>Vector2</code> object if and only if such an enemy exists, that the distance between them and the
	 * 	 * <code>AIPlayer</code> is less than 250 pixels, otherwise returns <code>null</code>
	 */
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

	/**
	 * Checks if the <code>Vector2</code> objects passed as arguments are roughly equal, that is, their absolute values
	 * do not differ more than 2 units
	 *
	 * @param current A <code>Vector2</code> object to check the vicinity of
	 * @param sought A <code>Vector2</code> object sought to be in the vicinity of
	 * @return <code>true</code> if and only if the absolute X and Y values of both <code>current</code> and
	 * <code>sought</code> <code>Vector2</code> objects do not differ more than 2 units, otherwise returns
	 * <code>false</code>
	 */
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
	//endregion

	//region Connection methods
	/**
	 * Listens for any incoming packets from possible clients, converts the received byte array to an object and
	 * passes the object to a server event listener for further processing
	 */
	private void connectionListener() {
		byte[] pokeBuf = new byte[NetworkingUtilities.objectToByteArray(new ConnectPacket()).length];
		var    incPoke = new DatagramPacket(pokeBuf, pokeBuf.length);

		try { clientSocket.receive(incPoke); } catch (SocketException e) {
			running = false;
			return;
		} catch (IOException e) {
			e.printStackTrace();
		}

		log(incPoke.getAddress() + " has connected");

		Object receivedPoke = NetworkingUtilities.byteArrayToObject(pokeBuf);

		if (receivedPoke == null) {
			System.err.println("Received packet is not a ConnectPacket object");
			return;
		}

		listenerServer.received(receivedPoke, incPoke.getAddress(), this, clientList);
	}

	/**
	 *  Checks whether the server is full, if not, creates a <code>UniquePortPacket</code>, iterates through the
	 *  <code>availablePositions</code> until it finds one that is occupied by a bot, removes the bot from the
	 *  <code>positionList</code>,
	 *  starts the <code>Connection</code> class on a new <code>Thread</code>, which sends the unique port to the
	 *  client, and adds the player to the <code>connectedList</code>
	 *
	 * @param address <code>InetAddress</code> of the client to be added to the game
	 */
	public synchronized void addConnection(InetAddress address) {
		//region Checks if Server is full
		if (clientList.connectedList.size() >= startPositions.length) {
			log("Server is full. Player " + address + " disconnected");
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

		Connection connection = new Connection(address, clientPort, portPacket, this, clientList);
		new Thread(connection).start();

		clientList.connectedList.put(address, connection);
		clientPort++;
	}

	/**
	 * Removes the player from positionList and adds a bot in its stead
	 *
	 * @param address The address of the player to be removed
	 */
	public void removeConnection(InetAddress address) {
		log(address + " has disconnected");

		if (clientList.connectedList.size() == 1) {
			clientList.connectedList.get(address).close();
			clientList.connectedList.remove(address);
			exit();
			return;
		}

		welcoming = true;

		Vector2 spawnPoint = clientList.connectedList.get(address).getSpawnPoint();

		availablePositions.replace(spawnPoint, true);

		for (int i = 0; i < startPositions.length; i++) {
			if (!spawnPoint.equals(startPositions[i]))
				continue;

			String botName = botNamesList.get(i);

			clientList.positionList.remove(address.toString());
			clientList.connectedIPs.remove(address.toString());

			clientList.positionList.put(botName, botList.get(botName).getPacketToSend());
			clientList.connectedIPs.add(botName);
			botList.get(botName).setActive(true);

			break;
		}

		clientList.connectedList.get(address).close();
		clientList.connectedList.remove(address);
	}

	/**
	 * Multicasts any object passed to it as the first argument on a predefined IP address and a port, passed as the
	 * second argument.
	 *
	 * @param o Object to be multicasted
	 * @param mPort Port for the object to be multicasted on a predefined IP address
	 */
	public synchronized void multicastPacket(Object o, int mPort) {
		try {
			byte[] sendBuf = NetworkingUtilities.objectToByteArray(o);
			var    packet  = new DatagramPacket(sendBuf, sendBuf.length, multicastGroup, mPort);
			multicastSocket.send(packet);
		} catch (IOException ignored) {}
	}
	//endregion

	//region Bullet list methods
	/**
	 * Adds a <code>Bullet</code> object to the server's <code>bulletSet</code> hash set.
	 *
	 * @param toAdd <code>Bullet</code> to be added to the set
	 */
	private synchronized void addBullet(Bullet toAdd) {
		bulletSet.add(toAdd);
	}

	/**
	 * Removes a <code>Bullet</code> object from the server's <code>bulletSet</code> hash set.
	 *
	 * @param toRemove <code>Bullet</code> to be removed from the set
	 */
	private synchronized void removeBullet(Bullet toRemove) {
		bulletSet.remove(toRemove);
	}

	/**
	 * Returns the server's <code>bulletSet</code> hash set of <code>Bullets</code>
	 *
	 * @return <code>bulletSet</code> hash set of <code>Bullets</code>
	 */
	private synchronized HashSet<Bullet> getBulletSet() {
		return new HashSet<>(bulletSet);
	}
	//endregion

	/**
	 * Prints out a passed message in a yellow font
	 *
	 * @param msg <code>String</code> message to be printed
	 */
	private void log(String msg) {
		System.out.println("\u001B[33m" + msg + "\u001B[0m");
	}
}
