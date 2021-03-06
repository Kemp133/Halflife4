package com.halflife3.GameModes;

import com.halflife3.Controller.*;
import com.halflife3.Controller.Input.*;
import com.halflife3.GameObjects.*;
import com.halflife3.GameUI.*;
import com.halflife3.GameObjects.Interfaces.*;
import com.halflife3.GameObjects.*;
import com.halflife3.Networking.Client.Client;
import com.halflife3.Networking.NetworkingUtilities;
import com.halflife3.Networking.Packets.PositionPacket;
import com.halflife3.View.Camera;
import com.halflife3.View.MapRender;
import javafx.application.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.transform.*;
import javafx.stage.*;
import javafx.util.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static javafx.scene.input.KeyCode.*;

public class MainMode extends GameMode {
	protected double scoreLimit;

	public static final float STUN_DURATION          = ClientController.FPS * 3;
	public static final int   SHOT_SPEED             = 250;
	public static final float POWER_UP_DURATION      = 50;
	public static final int   NORMAL_SPEED           = 120;
	public static final int   QUICK_SPEED            = 240;
	public static final float NORMAL_RELOAD_DURATION = 50;
	public static final float QUICK_RELOAD_DURATION  = 10;

	//region Other variables
	public  Pane                    root;
	public  Player                  thisPlayer;
	private HashMap<String, Player> playerList;
	private HashMap<Integer, Image> scoreSprite;
	private Input                   input;
	private ProgressBar[]           stunBar;
	private ProgressBar             amoBar;
	private ProgressBar             PowerUpBar;
	private Ball                    ball;
	private Stage                   window;
	private GraphicsContext         graphicsContext;
	private ExecutorService         executor;
	private Client                  clientNetwork;
	private char                    side;
	public  int                     yourScore            = 0;
	public  int                     enemyScore           = 0;
	public  int                     mapWidth;
	private int                     mapHeight;
	private int                     rightEndOfScreen;
	private int                     leftEndOfScreen;
	private int                     bottomOfScreen;
	private int                     topOfScreen;
	private float                   speedup_duration     = 0;
	private float                   quickReload_duration = 0;
	private boolean                 resetting;
	//endregion

	public MainMode(String GameModeName, double score) {
		super(GameModeName);
		scoreLimit = score;
	}

	@Override
	public void initialise() {
		//region Networking
		clientNetwork = new Client();
		clientNetwork.joinMulticastGroup();
		clientNetwork.getHostInfo();
		clientNetwork.connectToServer();
		//endregion

		//region Map loading
		MapRender.LoadLevel();
		//endregion

		//region Initialise Objects
		input       = Input.getInstance();
		playerList  = new HashMap<>();
		scoreSprite = new HashMap<>();
		stunBar     = new ProgressBar[MapRender.getStartPositions().length];
		root        = new Pane();
		executor    = Executors.newSingleThreadExecutor();
		resetting   = false;
		//endregion

		//region Initialise This Player
		thisPlayer = new Player(clientNetwork.getStartingPosition());
		thisPlayer.setIpOfClient(clientNetwork.getClientAddress().toString());
		thisPlayer.setReloadTime(NORMAL_RELOAD_DURATION);
		thisPlayer.setMovementSpeed(NORMAL_SPEED);
		//endregion

		//region Set The Distances To The Sides Of The Window
		rightEndOfScreen = (int) (WindowAttributes.GAME_WINDOW_WIDTH / 2 + thisPlayer.getWidth());
		leftEndOfScreen  = (int) (WindowAttributes.GAME_WINDOW_WIDTH / 2 - thisPlayer.getWidth());
		topOfScreen      = (int) ((WindowAttributes.GAME_WINDOW_HEIGHT - thisPlayer.getHeight()) / 2);
		bottomOfScreen   = (int) ((WindowAttributes.GAME_WINDOW_HEIGHT + thisPlayer.getHeight()) / 2);
		//endregion

		//region Wait For Server To Acknowledge Player Connection
		do {
			clientNetwork.receivePositions();
		} while (!clientNetwork.listOfClients.connectedIPs.contains(thisPlayer.getIpOfClient()));
		//endregion

		//region Initialise The Other Players
		initialisePlayers();
		//endregion

		//region Set Up Scene
		Canvas canvas = new Canvas(WindowAttributes.GAME_WINDOW_WIDTH, WindowAttributes.GAME_WINDOW_HEIGHT);
		root.getChildren().add(canvas);
		graphicsContext = canvas.getGraphicsContext2D();

		Scene scene = new Scene(root, WindowAttributes.GAME_WINDOW_WIDTH, WindowAttributes.GAME_WINDOW_HEIGHT);
		SceneManager.getInstance().setScene("Game Scene", scene);
		window = SceneManager.getInstance().getMainWindow();
		//endregion

		//region Initialise Game Objects
		gameInit();
		//endregion

		//region Set Side Of Player
		side = (thisPlayer.getPosX() < mapWidth / 2f) ? 'L' : 'R';
		//endregion

		//region Initialise Stun Bars
		for (int i = 0; i < MapRender.getStartPositions().length; i++) {
			stunBar[i] = new ProgressBar(0);
			stunBar[i].setStyle("-fx-accent: green;");
			stunBar[i].setPrefHeight(8);
			stunBar[i].setPrefWidth(40);
			root.getChildren().add(stunBar[i]);
		}
		//endregion

		//region Initialise Ammo Bar
		amoBar = new ProgressBar(1);
		amoBar.setStyle("-fx-accent: red;");
		amoBar.setPrefHeight(20);
		amoBar.setPrefWidth(150);
		root.getChildren().add(amoBar);
		//endregion

		//region Initialise Power up Bar
		PowerUpBar = new ProgressBar(0);
		PowerUpBar.setStyle("-fx-accent: blue;");
		PowerUpBar.setPrefHeight(20);
		PowerUpBar.setPrefWidth(150);
		root.getChildren().add(PowerUpBar);
		//endregion

		//region Initialise Ball
		PositionPacket ballPacket = clientNetwork.listOfClients.posList.get("ball");
		ball = new Ball(new Vector2(ballPacket.posX, ballPacket.posY));
		//endregion

		//region Thread To Update Position Of All Enemies and The Ball
		executor.submit(() -> {
			while (true)
				updateEnemies();
		});
		//endregion

		System.out.println("Game Running");
	}

	@Override
	public void gameLoop(double elapsedTime) {
		if (resetting)
			return;

		//region Camera offset
		Camera.SetOffsetX(thisPlayer.getPosX() - leftEndOfScreen);
		Camera.SetOffsetY(thisPlayer.getPosY() - topOfScreen);
		if (Camera.GetOffsetX() < 0)
			Camera.SetOffsetX(0);
		else if (Camera.GetOffsetX() > mapWidth - leftEndOfScreen - rightEndOfScreen)
			Camera.SetOffsetX(mapWidth - leftEndOfScreen - rightEndOfScreen);
		if (Camera.GetOffsetY() < 0)
			Camera.SetOffsetY(0);
		else if (Camera.GetOffsetY() > mapHeight - topOfScreen - bottomOfScreen)
			Camera.SetOffsetY(mapHeight - topOfScreen - bottomOfScreen);
		//endregion

		//region Calculate the rotation
		Vector2 playerClientCenter = new Vector2(thisPlayer.getPosX() - Camera.GetOffsetX() + thisPlayer.getWidth() / 2,
				thisPlayer.getPosY() - Camera.GetOffsetY() + thisPlayer.getHeight() / 2);
		Vector2 direction = new Vector2(input.getMousePosition().getX(), input.getMousePosition().getY()).subtract(playerClientCenter);

		Affine rotate = new Affine();
		short  deg    = (short) Math.toDegrees(Math.atan2(direction.getY(), direction.getX()));
		rotate.appendRotation(deg, playerClientCenter.getX(), playerClientCenter.getY());
		thisPlayer.setDegrees(deg);
		thisPlayer.setAffine(rotate);
		//endregion

		//region Sets player's velocity according to input
		if (thisPlayer.stunned == 0) {
			Vector2 toSet = new Vector2();
			if (input.isKeyPressed(A))
				toSet.add(-thisPlayer.getMovementSpeed(), 0);
			if (input.isKeyPressed(D))
				toSet.add(thisPlayer.getMovementSpeed(), 0);
			if (input.isKeyPressed(W))
				toSet.add(0, -thisPlayer.getMovementSpeed());
			if (input.isKeyPressed(S))
				toSet.add(0, thisPlayer.getMovementSpeed());

			thisPlayer.setVelocity(toSet);
		}
		//endregion

		//region Collision detection
//		  Player collision
		for (Brick block : MapRender.GetList())
			if (block.getBounds().intersects(thisPlayer.circle.getBoundsInLocal()))
				thisPlayer.collision(block, elapsedTime);

		bulletCollision();
		//endregion

		//region Check power ups
		//region Speed-Up
		if (speedup_duration != 0)
			speedup_duration--;
		else
			thisPlayer.setMovementSpeed(NORMAL_SPEED);

		for (Speedup su : MapRender.getSpeedup_list()) {
			if (!thisPlayer.getBounds().intersects(su.getBounds().getBoundsInLocal()))
				continue;

			speedup_duration = POWER_UP_DURATION;
			thisPlayer.setMovementSpeed(QUICK_SPEED);
			MapRender.getSpeedup_list().remove(su);
			su.destroy();
			break;
		}
		//endregion

		//region Quick-Reload
		if (quickReload_duration != 0)
			quickReload_duration--;
		else
			thisPlayer.setReloadTime(NORMAL_RELOAD_DURATION);

		for (FastReload fd : MapRender.getFastReload_list()) {
			if (!thisPlayer.getBounds().intersects(fd.getBounds().getBoundsInLocal()))
				continue;

			quickReload_duration = POWER_UP_DURATION;
			thisPlayer.setReloadTime(QUICK_RELOAD_DURATION);
			MapRender.getFastReload_list().remove(fd);
			fd.destroy();
			break;
		}
		//endregion
		//endregion

		//region Updates position of all game objects locally (has to go after collision)
		for (IUpdateable go : ObjectManager.getGameObjects())
			go.update(elapsedTime);
		//endregion

		//region Checks if the player is holding the ball
		boolean playerIsTouchingTheBall = ball.getBounds().intersects(thisPlayer.circle.getBoundsInLocal());
		if (playerIsTouchingTheBall && !ball.isHeld && thisPlayer.stunned == 0)
			thisPlayer.setHoldsBall(true);
		//endregion

		//region Shoots a bullet or the ball
		thisPlayer.setBulletShot(false);
		if (input.isButtonPressed(MouseButton.PRIMARY) && thisPlayer.reload == thisPlayer.getReloadTime() && thisPlayer.stunned == 0) {
			double  bulletX      = Math.cos(Math.atan2(direction.getY(), direction.getX()));
			double  bulletY      = Math.sin(Math.atan2(direction.getY(), direction.getX()));
			Vector2 shotVelocity = new Vector2(bulletX, bulletY).multiply(SHOT_SPEED);

			if (thisPlayer.isHoldingBall()) { // Shoots the ball
				boolean ballInWall = false;
				for (Brick block : MapRender.GetList())
					if (ball.getBounds().intersects(block.getBounds().getBoundsInLocal())) {
						ballInWall = true;
						break;
					}

				thisPlayer.setBulletShot(!ballInWall);
			} else { // Shoots a bullet
				Vector2 gunDirection = new Vector2(bulletX * 32, bulletY * 32);
				Vector2 bulletPos = new Vector2(thisPlayer.getPosX() + thisPlayer.getHeight() / 2,
						thisPlayer.getPosY() + thisPlayer.getWidth() / 2).add(gunDirection);

				new Bullet(bulletPos, shotVelocity, thisPlayer.getIpOfClient());
				thisPlayer.setBulletShot(true);
			}

			thisPlayer.reload = 0;
		}
		//endregion

		//region Re-renders all game objects
		RenderManager.getInstance().render(graphicsContext);
		//endregion

		//region Updates the stun bar
		int id = 0;
		for (var player : playerList.values()) {
			stunBar[id].setLayoutX(player.getPosX() - Camera.GetOffsetX());
			stunBar[id].setLayoutY(player.getPosY() - Camera.GetOffsetY() - 12);
			stunBar[id].setProgress(player.stunned / STUN_DURATION);
			id++;
		}
		//endregion

		//region Updates the reload bar
		amoBar.setProgress(thisPlayer.reload / thisPlayer.getReloadTime());
		amoBar.setLayoutX(40);
		amoBar.setLayoutY(40);
		//endregion

		//region Updates the power up bar
		PowerUpBar.setProgress(Math.max(speedup_duration, quickReload_duration) / POWER_UP_DURATION);
		PowerUpBar.setLayoutX(40);
		PowerUpBar.setLayoutY(80);
		//endregion

		//region Checks if a goal has been scored
		checkForGoal();
		//endregion

		//region Renders the score text
		graphicsContext.drawImage(scoreSprite.get(yourScore), WindowAttributes.GAME_WINDOW_WIDTH / 2f - 40, 40);
		graphicsContext.drawImage(scoreSprite.get(-1), WindowAttributes.GAME_WINDOW_WIDTH / 2f + 10, 40);
		graphicsContext.drawImage(scoreSprite.get(enemyScore), WindowAttributes.GAME_WINDOW_WIDTH / 2f + 40, 40);
		//endregion

		//region End Condition
		if (won() || lost()) {
			if (won())
				win = true;
			finished();
			return;
		}
		//endregion

		//region Sends the client's position, whether they've shot a bullet and if they're holding the ball
		if (thisPlayer.stunned != 0)
			thisPlayer.setHoldsBall(false);

		clientNetwork.sendPacket(thisPlayer.getPacketToSend(), clientNetwork.getUniquePort());

		if (thisPlayer.isBulletShot())
			thisPlayer.setHoldsBall(false);
		//endregion
	}

	@Override
	public void finished() {
//		Log game win or loss in leaderboard
		if (yourScore == scoreLimit) { //Can't just call won() right?
			updateLBoard(DatabaseManager.getConnection(), BaseController.GetApplicationUser().username);
		}

//		Send packet to end the game
//		Set up game to await for the won/lost packet
//		region Showing Who Won
//				VBox  vbox  = new VBox();
//				Text text  = new Text("Team " + ((yourScore == scoreLimit) ? side : (side == 'L' ? 'R' : 'L')) +
//				"won!");
//				vbox.getChildren().add(text);
//				Scene wonScene = new Scene(vbox, 800, 600, Color.WHITE);
//				window = new Stage();
//				window.setScene(wonScene);
//				//endregion

		hasFinished = true;
		ObjectManager.resetObjects();
//		RenderManager.getInstance().removeAllObjects();
		System.out.println("Game exited");
		executor.shutdownNow();
		clientNetwork.disconnect();
	}

	@Override
	public boolean won() {
		//Send everybody else the game over packet
		return yourScore == scoreLimit;
	}

	@Override
	public boolean lost() {
		return enemyScore == scoreLimit;
	}

	private void checkForGoal() {
		Goal g = goalScored();
		if (g == null)
			return;

		resetting         = true;
		thisPlayer.reload = 0;

		if (g.getScoringTeam() == side) {
			yourScore++;
			System.out.println("Goal for YOUR team!");
		} else {
			enemyScore++;
			System.out.println("Goal for the ENEMY team!");
		}

		thisPlayer.resetBasics();
		ball.reset();

		NetworkingUtilities.WaitXSeconds(1);

		new Thread(() -> {
			NetworkingUtilities.WaitXSeconds(1);
			resetting = false;
		}).start();
	}

	private Goal goalScored() {
		for (Goal g : MapRender.getGoalZone())
			if (ball.getBounds().intersects(g.getBounds().getBoundsInLocal()))
				return g;

		return null;
	}

	/** A method to initialise the players in the game */
	public void initialisePlayers() {
		clientNetwork.receivePositions();
		for (String ip : clientNetwork.listOfClients.connectedIPs) {
			if (ip.equals(thisPlayer.getIpOfClient())) {
				playerList.put(ip, thisPlayer);
				continue;
			}

			PositionPacket theDoubleValues = clientNetwork.listOfClients.posList.get(ip);
			Player         enemy           = new Player(new Vector2(theDoubleValues.posX, theDoubleValues.posY));
			enemy.setIpOfClient(ip);
			playerList.put(ip, enemy);
		}
	}

	private void gameInit() {
		//region Background setup
		root.setBackground(MenuUtilitites.getBackground(getClass(), "Sprites/Background/Space.png"));
		//endregion

		//region Add audio into game
		AudioForGame audio = new AudioForGame();
		audio.getMenu().getItems().add(audio.getMute());
		audio.getSlider1().setHideOnClick(false);
		audio.getMenu().getItems().add(audio.getSlider1());
		audio.getMenuBar().getMenus().add(audio.getMenu());
		audio.getBattle_music().setAutoPlay(true);
		audio.getBattle_music().setMute(true); //Mute music by default
		audio.getBattle_music().setOnEndOfMedia(() -> {
			audio.getBattle_music().seek(Duration.ZERO);
			audio.getBattle_music().play();
		});
		audio.getMute().setOnAction(actionEvent -> audio.switchMute());
		audio.getSlider1().setOnAction(actionEvent -> audio.volumeControl(audio.getVolume()));
		//root.getChildren().add(audio.getMenuBar());
		//endregion

		//region Key input listener setup
		root.addEventHandler(KeyEvent.ANY, new KeyboardInput());
		root.addEventHandler(MouseEvent.ANY, new MouseInput());
		//endregion

		//region Gets width and height of the map
		try {
			Image map = new Image(new FileInputStream(Maps.Map));
			mapWidth  = (int) map.getWidth() * MapRender.BLOCK_SIZE;
			mapHeight = (int) map.getHeight() * MapRender.BLOCK_SIZE;
		} catch (IOException e) { e.printStackTrace(); }
		//endregion

		//region Adds score sprites
		scoreSprite.put(-1, new Image(getClass().getClassLoader().getResourceAsStream("Sprites/Score/vs.png")));
		scoreSprite.put(0, new Image(getClass().getClassLoader().getResourceAsStream("Sprites/Score/0.png")));
		scoreSprite.put(1, new Image(getClass().getClassLoader().getResourceAsStream("Sprites/Score/1.png")));
		scoreSprite.put(2, new Image(getClass().getClassLoader().getResourceAsStream("Sprites/Score/2.png")));
		scoreSprite.put(3, new Image(getClass().getClassLoader().getResourceAsStream("Sprites/Score/3.png")));
		//endregion

		//region Pause menu
		root.setOnKeyPressed(keyEvent -> {
			if (keyEvent.getCode() != KeyCode.ESCAPE)
				return;

			//Blurs the background
			root.setEffect(new GaussianBlur());

			//VBox - pane with buttons
			VBox pauseRoot = new VBox(6);
			pauseRoot.setStyle("-fx-background-color: rgba(255, 255, 255, 0.8);");
			pauseRoot.setAlignment(Pos.CENTER);
			pauseRoot.setPadding(new Insets(20));
			pauseRoot.getChildren().add(new Label("Paused"));

			//Stage of the pause menu
			Stage popupStage = new Stage(StageStyle.TRANSPARENT);
			popupStage.initOwner(window);
			popupStage.initModality(Modality.APPLICATION_MODAL);
			popupStage.setScene(new Scene(pauseRoot, Color.TRANSPARENT));

			//region ReturnToGame button
			Button returnToGame = new Button("Return to game");
			pauseRoot.getChildren().add(returnToGame);
			returnToGame.setOnAction(event -> {
				root.setEffect(null);
				popupStage.hide();
			});
			//endregion

			//region Audio button
			Button sound = new Button("Audio On/Off");
			pauseRoot.getChildren().add(sound);
			sound.setOnAction(actionEvent -> audio.switchMute());
			//endregion

			//region ToMainMenu button
			Button toMainMenu = new Button("Quit to Main Menu");
			pauseRoot.getChildren().add(toMainMenu);
			toMainMenu.setOnAction(actionEvent -> {
				root.setEffect(null);
				popupStage.hide();
				finished();
			});
			//endregion

			//region ToDesktop button
			Button toDesktop = new Button("Quit to Desktop");
			pauseRoot.getChildren().add(toDesktop);
			toDesktop.setOnAction(actionEvent -> {
				Platform.exit();
				System.exit(0);
			});
			//endregion

			popupStage.show();
		});
		//endregion
	}

	public void updateEnemies() {
		clientNetwork.receivePositions();

		//region Replaces Bots <-> Players
		ArrayList<String> playerKeys          = new ArrayList<>(playerList.keySet());
		boolean           aNewPlayerHasJoined = false;

		for (String playerName : playerKeys) {
//            If bot name/player IP is stored locally - continue
			if (clientNetwork.listOfClients.connectedIPs.contains(playerName))
				continue;

//            If server list has been updated - reset the odd player's position and velocity
			playerList.get(playerName).resetBasics();
			aNewPlayerHasJoined = true;

//            Find the odd player (bot or disconnected player)
			for (String newIP : clientNetwork.listOfClients.connectedIPs) {
//                If the player is in both local (checked before) and server lists - continue
				if (playerList.containsKey(newIP))
					continue;

//                If newIP is in the server list but not in the local list
				playerList.get(playerName).setIpOfClient(newIP); //Change the Player's IP to newIP
				playerList.put(newIP, playerList.get(playerName)); //Copy the old player (with changed IP) to the list
				playerList.remove(playerName); //Delete the old player entry from local list
				break;
			}
			break;
		}
		//endregion

		//region Resets the position of the ball and all the players if a new player has joined the game
		if (aNewPlayerHasJoined) {
			ball.reset();
			for (Player player : playerList.values())
				player.resetBasics();
			return;
		}
		//endregion

		//region Updates info of *other* players/bots and the ball
		for (String ip : clientNetwork.listOfClients.posList.keySet()) {
			if (ip.equals(thisPlayer.getIpOfClient()))
				continue;

			PositionPacket theDoubleValues = clientNetwork.listOfClients.posList.get(ip);
			Player         enemy           = playerList.get(ip);

			//region Rotation / Position / Velocity
			if (!ip.equals("ball")) {
				Affine rotate = new Affine();
				rotate.appendRotation(theDoubleValues.degrees, theDoubleValues.posX - Camera.GetOffsetX() + thisPlayer.getWidth() / 2,
						theDoubleValues.posY - Camera.GetOffsetY() + thisPlayer.getHeight() / 2);

				enemy.setAffine(rotate);
				enemy.setPosition(theDoubleValues.posX, theDoubleValues.posY);
				enemy.setVelocity(theDoubleValues.velX, theDoubleValues.velY);
			} else {
				ball.setPosition(theDoubleValues.posX, theDoubleValues.posY);
				ball.setVelocity(theDoubleValues.velX, theDoubleValues.velY);
				ball.isHeld = theDoubleValues.holdsBall;
				continue;
			}
			//endregion

			//region Enemies' bullet shots
			if (!theDoubleValues.bulletShot)
				continue;

			double  degreeRadians = Math.toRadians(theDoubleValues.degrees);
			double  bulletX       = Math.cos(degreeRadians);
			double  bulletY       = Math.sin(degreeRadians);
			Vector2 shotVel       = new Vector2(bulletX, bulletY).multiply(NORMAL_SPEED * 2);
			Vector2 gunDirection  = new Vector2(bulletX * 32, bulletY * 32);
			Vector2 bulletPos = new Vector2(theDoubleValues.posX + thisPlayer.getHeight() / 2,
					theDoubleValues.posY + thisPlayer.getWidth() / 2).add(gunDirection);

			new Bullet(bulletPos, shotVel, "enemy");
			theDoubleValues.bulletShot = false;
			//endregion
		}
		//endregion
	}

	private void bulletCollision() {
		HashSet<GameObject> bulletsToDestroy = new HashSet<>();
		for (GameObject bullet : ObjectManager.getGameObjects()) {
			if (!bullet.getKeys().contains("Bullet"))
				continue;

//			  Bullets and Walls
			for (Brick block : MapRender.GetList())
				if (bullet.getBounds().intersects(block.getBounds().getBoundsInLocal()))
					bulletsToDestroy.add(bullet);

//			  Bullets and Players
			for (String ip : clientNetwork.listOfClients.connectedIPs) {
				if (((Bullet) bullet).getShooterName().equals(ip))
					continue;

				Player player = playerList.get(ip);

				if (!bullet.getBounds().intersects(player.circle.getBoundsInLocal()))
					continue;

				bulletsToDestroy.add(bullet);

				if (player.stunned != 0)
					continue;

				player.stunned = STUN_DURATION;
				player.setVelocity(bullet.getVelocity().divide(STUN_DURATION / ClientController.FPS));
				player.setDeceleration(new Vector2(player.getVelocity()).divide(ClientController.FPS / 2f));
				player.setHoldsBall(false);
			}
		}

		for (GameObject bullet : bulletsToDestroy)
			bullet.destroy();
	}

	private void updateLBoard(Connection c, String username) {
		PreparedStatement preparedStatement = null;
		int               score             = currentScore(DatabaseManager.getConnection(), username);
		try {
			String query = "UPDATE userdatascore SET score = " + (++score) + " WHERE name = '" + username + "'";
			preparedStatement = c.prepareStatement(query);

			int rowAffected = preparedStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DatabaseManager.closeConnections(c, preparedStatement, null);
		}
	}

	private int currentScore(Connection c, String username) {
		PreparedStatement preparedStatement = null;
		ResultSet         rs                = null;
		try {
			//Creating a query checking if username is in the table
			String query = "SELECT * FROM userdatascore WHERE name = '" + username + "'";
			//Creating the Statement
			preparedStatement = c.prepareStatement(query);
			//Executing the query
			rs = preparedStatement.executeQuery();
			//Returns the score for the user
			if (rs.next()) {
				return rs.getInt("score");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DatabaseManager.closeConnections(c, preparedStatement, rs);
		}
		return 0;
	}
}