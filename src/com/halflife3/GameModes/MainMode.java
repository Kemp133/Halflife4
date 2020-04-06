package com.halflife3.GameModes;

import com.halflife3.Controller.ClientController;
import com.halflife3.Controller.Input.Input;
import com.halflife3.Controller.Input.KeyboardInput;
import com.halflife3.Controller.Input.MouseInput;
import com.halflife3.Controller.ObjectManager;
import com.halflife3.Controller.SceneManager;
import com.halflife3.GameUI.AudioForGame;
import com.halflife3.GameUI.Maps;
import com.halflife3.GameObjects.*;
import com.halflife3.Mechanics.Interfaces.IRenderable;
import com.halflife3.Mechanics.Interfaces.IUpdateable;
import com.halflife3.Mechanics.Vector2;
import com.halflife3.Networking.Client.Client;
import com.halflife3.Networking.NetworkingUtilities;
import com.halflife3.Networking.Packets.PositionPacket;
import com.halflife3.Networking.Server.Server;
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

import static javafx.scene.input.KeyCode.*;

public class MainMode extends GameMode {
	protected double scoreLimit;

	private static final int   GAME_WINDOW_HEIGHT = 600;
	private static final int   GAME_WINDOW_WIDTH  = 800;
	private static final int   MOVEMENT_SPEED     = 120;
	public static final  int   SHOT_SPEED         = 200;
	public static final  float STUN_DURATION      = 15;
	public static final  float RELOAD_DURATION    = 50;

	//region Other variables
	public  Pane                    root;
	private Scene                   scene;
	public  Player                  thisPlayer;
	private HashMap<String, Player> playerList;
	private HashMap<Integer, Image> scoreSprite;
	private Input                   input         = Input.getInstance();
	private ProgressBar[]           stunBar;
	private ProgressBar             amoBar;
	private Ball                    ball;
	private Stage                   window        = null;
	private char                    side;
	public  int                     yourScore     = 0;
	public  int                     enemyScore    = 0;
	public  boolean                 running       = false;
	private int                     bulletLimiter = 0;
	public  int                     mapWidth;
	private int                     mapHeight;
	private GraphicsContext         graphicsContext;
	private double                  ballPreviousX;
	private int                     rightEndOfScreen;
	private int                     leftEndOfScreen;
	private int                     bottomOfScreen;
	private int                     topOfScreen;
	//endregion

	//	public MainMode() {}

	public MainMode(String GameModeName, double score) {
		super(GameModeName);
		scoreLimit = score;
	}

	@Override
	public void initialise() {
		//region Networking
		Client clientNetwork = new Client();
		clientNetwork.joinGroup();
		clientNetwork.getHostInfo();
		clientNetwork.start();
		//endregion

		//region Initialise Objects
		playerList  = new HashMap<>();
		scoreSprite = new HashMap<>();
		stunBar     = new ProgressBar[4];
		root        = new Pane();
		//endregion

		//region Initialise This Player
		thisPlayer = new Player(clientNetwork.getStartingPosition());
		thisPlayer.setIpOfClient(clientNetwork.getClientAddress().toString());
		//endregion

		//region Set The Distances To The Sides Of The Window
		rightEndOfScreen = (int) (GAME_WINDOW_WIDTH / 2 + thisPlayer.getWidth());
		leftEndOfScreen  = (int) (GAME_WINDOW_WIDTH / 2 - thisPlayer.getWidth());
		topOfScreen      = (int) ((GAME_WINDOW_HEIGHT - thisPlayer.getHeight()) / 2);
		bottomOfScreen   = (int) ((GAME_WINDOW_HEIGHT + thisPlayer.getHeight()) / 2);
		//endregion

		//region Wait For Server To Acknowledge Player Connection
		do {
			Client.receivePositions();
		} while (!Client.listOfClients.connectedIPs.contains(thisPlayer.getIpOfClient()));
		//endregion

		//region Initialise The Other Players
		initialisePlayers();
		//endregion

		//region Set Up Scene
		Canvas canvas = new Canvas(GAME_WINDOW_WIDTH, GAME_WINDOW_HEIGHT);
		root.getChildren().add(canvas);
		graphicsContext = canvas.getGraphicsContext2D();

		scene = new Scene(root, GAME_WINDOW_WIDTH, GAME_WINDOW_HEIGHT);
		SceneManager.getInstance().setScene("Game Scene", scene);
		window = SceneManager.getInstance().getMainWindow();
		//endregion

		//region Initialise Game Objects
		gameInit(scene);
		//endregion

		//region Set Side Of Player
		side = (thisPlayer.getPosX() < mapWidth / 2f) ? 'L' : 'R';
		//endregion

		//region Initialise Stun Bars
		for (int i = 0; i < Server.startPositions.length; i++) {
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

		//region Initialise Ball
		ball          = new Ball(new Vector2(mapWidth / 2f, mapHeight / 2f), "ClientBall");
		ballPreviousX = ball.getPosX();
		//endregion

		//region Thread To Update Position Of All Enemies and The Ball
		running = true;
		new Thread(() -> {
			while (running)
				updateEnemies();
		}).start();
		//endregion

		System.out.println("Game Running");
	}

	@Override
	public void gameLoop(double elapsedTime) {
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
		Vector2 playerClientCenter =
				new Vector2(thisPlayer.getPosX() - Camera.GetOffsetX() + thisPlayer.getWidth() / 2,
				thisPlayer.getPosY() - Camera.GetOffsetY() + thisPlayer.getHeight() / 2);
		Vector2 direction = new Vector2(input.getMousePosition().getX(), input.getMousePosition().getY()).subtract(
				playerClientCenter);

		Affine rotate = new Affine();
		short  deg    = (short) Math.toDegrees(Math.atan2(direction.getY(), direction.getX()));
		rotate.appendRotation(deg, playerClientCenter.getX(), playerClientCenter.getY());
		thisPlayer.setDegrees(deg);
		thisPlayer.setAffine(rotate);
		//endregion

		//region Handles player movement
		if (thisPlayer.stunned == 0) {
			if (input.isKeyReleased(A) && input.isKeyReleased(D)) {
				thisPlayer.getVelocity().setX(0);
			}
			if (input.isKeyReleased(W) && input.isKeyReleased(S)) {
				thisPlayer.getVelocity().setY(0);
			}
			if (input.isKeyPressed(A)) {
				thisPlayer.getVelocity().setX(-MOVEMENT_SPEED);
			}
			if (input.isKeyPressed(D)) {
				thisPlayer.getVelocity().setX(MOVEMENT_SPEED);
			}
			if (input.isKeyPressed(W)) {
				thisPlayer.getVelocity().setY(-MOVEMENT_SPEED);
			}
			if (input.isKeyPressed(S)) {
				thisPlayer.getVelocity().setY(MOVEMENT_SPEED);
			}
		}
		//endregion

		//region Collision detection
//		Player collision
		for (Bricks block : MapRender.GetList())
			if (block.getBounds().intersects(thisPlayer.circle.getBoundsInLocal()))
				thisPlayer.collision(block, elapsedTime);

//		  Bullet collision
		editObjectManager(1, 0, null, null, null);
		//endregion

		//region Updates position of all game objects locally (has to go after collision)
		editObjectManager(2, elapsedTime, null, null, null);
		//endregion

		//region Checks if the player is holding the ball
		boolean playerIsTouchingTheBall = ball.getBounds().intersects(thisPlayer.circle.getBoundsInLocal());
		if (playerIsTouchingTheBall && !ball.isHeld)
			thisPlayer.setHoldsBall(true);
		//endregion

		//region Shoots a bullet or the ball
		thisPlayer.setBulletShot(false);
		if (input.isButtonPressed(MouseButton.PRIMARY) && bulletLimiter == 0) {
			double  bulletX      = Math.cos(Math.atan2(direction.getY(), direction.getX()));
			double  bulletY      = Math.sin(Math.atan2(direction.getY(), direction.getX()));
			Vector2 shotVelocity = new Vector2(bulletX, bulletY).multiply(SHOT_SPEED);

			if (thisPlayer.isHoldingBall()) { // Shoots the ball
				boolean ballInWall = false;
				for (Bricks block : MapRender.GetList())
					if (ball.getBounds().intersects(block.getBounds().getBoundsInLocal())) {
						ballInWall = true;
						break;
					}

				thisPlayer.setBulletShot(!ballInWall);
			} else if (thisPlayer.reload == RELOAD_DURATION) { // Shoots a bullet
				Vector2 gunDirection = new Vector2(bulletX * 32, bulletY * 32);
				Vector2 bulletPos = new Vector2(thisPlayer.getPosX() + thisPlayer.getHeight() / 2,
						thisPlayer.getPosY() + thisPlayer.getWidth() / 2).add(gunDirection);

				editObjectManager(0, 0, bulletPos, shotVelocity, thisPlayer.getIpOfClient());
				thisPlayer.setBulletShot(true);
				thisPlayer.reload = 0;
			}
			bulletLimiter = ClientController.FPS / 5;
		} else if (bulletLimiter > 0)
			bulletLimiter--;
		//endregion

		//region Re-renders all game objects
		graphicsContext.clearRect(0, 0, GAME_WINDOW_WIDTH, GAME_WINDOW_HEIGHT);

		MapRender.Render(graphicsContext);

		for (IRenderable go : ObjectManager.getGameObjects()) {
			if (go instanceof Ball)
				if (((Ball) go).containsKey("ServerBall"))
					continue;
			go.render(graphicsContext);
		}

		//endregion

		//region Updates the stun bar
		int id = 0;
		for (String ip : playerList.keySet()) {
			stunBar[id].setLayoutX(playerList.get(ip).getPosX() - Camera.GetOffsetX());
			stunBar[id].setLayoutY(playerList.get(ip).getPosY() - Camera.GetOffsetY() - 12);
			stunBar[id].setProgress(playerList.get(ip).stunned / (STUN_DURATION * 5));
			id++;
		}
		//endregion

		//region Updates the reload bar
		amoBar.setProgress(thisPlayer.reload / RELOAD_DURATION);
		amoBar.setLayoutX(40);
		amoBar.setLayoutY(40);
		//endregion

		//region Sends the client's position, whether they've shot a bullet and if they're holding the ball
		if (thisPlayer.stunned != 0)
			thisPlayer.setHoldsBall(false);
		Client.sendPacket(thisPlayer.getPacketToSend(), Client.getUniquePort());
		if (thisPlayer.isBulletShot())
			thisPlayer.setHoldsBall(false);
		//endregion

		//region Checks if a goal has been scored
		if (ballPreviousX - ball.getPosX() < -mapWidth / 4f) {
			scored('R');
		} else if (ballPreviousX - ball.getPosX() > mapWidth / 4f) {
			scored('L');
		}
//		if((ballPreviousX>Server.GOAL_WIDTH+40||ballPreviousX<mapWidth-Server.GOAL_WIDTH-40) && (ball.getPosX()
//		-ballPreviousX>40||ball.getPosX()-ballPreviousX<-40)){
//			yourScore = 0;
//			enemyScore = 0;
//			thisPlayer.reset();
//		}
		ballPreviousX = ball.getPosX();
		//endregion

		//region Renders the score text
		graphicsContext.drawImage(scoreSprite.get(yourScore), GAME_WINDOW_WIDTH / 2f - 40, 40);
		graphicsContext.drawImage(scoreSprite.get(-1), GAME_WINDOW_WIDTH / 2f + 10, 40);
		graphicsContext.drawImage(scoreSprite.get(enemyScore), GAME_WINDOW_WIDTH / 2f + 40, 40);
		//endregion

		if (won() || lost() || hasFinished) {
			finished();
			hasFinished = true;
		}
	}

	@Override
	public void finished() {
//		Log game win or loss in leaderboard
//				if (yourScore == scoreLimit) { //Can't just call won() right?
//					updateLBoard(getConnection(), BaseController.GetApplicationUser().username);
//				}
//
//		Send packet to end the game
//		Set up game to await for the won/lost packet
//
//		region Showing Who Won
//				VBox  vbox  = new VBox();
//				Text  text  = new Text("Team " + ((yourScore == scoreLimit) ? side : (side == 'L' ? 'R' : 'L')) +
//				"won!");
//				vbox.getChildren().add(text);
//				Scene wonScene = new Scene(vbox, 800, 600, Color.WHITE);
//				window = new Stage();
//				window.setScene(wonScene);
//				//endregion

		scene.setCursor(Cursor.DEFAULT);

//				try {
//					SceneManager.getInstance().restorePreviousScene();
//				} catch (SceneStackEmptyException e) {
//					NetworkingUtilities.CreateErrorMessage(
//							"Scene stack empty",
//							"The scene stack only contained one element",
//							"It is impossible to backtrack, error created in '" + getClass().getName() + "'"
//					);
//				}

		System.out.println("Game exited");
		running = false;
		Client.disconnect();
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

	/** A method to initialise the players in the game */
	public void initialisePlayers() {
		Client.receivePositions();
		for (String ip : Client.listOfClients.connectedIPs) {
			if (ip.equals(thisPlayer.getIpOfClient())) {
				playerList.put(ip, thisPlayer);
				continue;
			}

			PositionPacket theDoubleValues = Client.listOfClients.posList.get(ip);
			Player enemy = new Player(new Vector2(theDoubleValues.posX, theDoubleValues.posY));
			enemy.setIpOfClient(ip);
			playerList.put(ip, enemy);
		}
	}

	private void gameInit(Scene scene) {
		//region Background setup
		try {
			Image image  = new Image(new FileInputStream("res/Space.png"));
			var   bgSize = new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true);
			var   myBI   = new BackgroundImage(image, null, null, null, bgSize);
			root.setBackground(new Background(myBI));
		} catch (FileNotFoundException e) {
			System.out.println("Could not find file in path: 'res/Space.png'");
			NetworkingUtilities.CreateErrorMessage("Error Loading Background Image",
					"The background image could not " + "be loaded!", "Exception message: " + e.getMessage());
		}
		//endregion

		//region Initialise cursor
		new Crosshair(input.getMousePosition(), new Vector2(0, 0));
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
		root.getChildren().add(audio.getMenuBar());
		//endregion

		//region Key input listener setup
		root.addEventHandler(KeyEvent.ANY, new KeyboardInput());
		root.addEventHandler(MouseEvent.ANY, new MouseInput());
		scene.setCursor(Cursor.NONE);
		//endregion

		//region Map loading
		MapRender.LoadLevel();
		//endregion

		//region Gets width and height of the map
		try {
			Image map = new Image(new FileInputStream(Maps.Map));
			mapWidth  = (int) map.getWidth() * MapRender.BLOCK_SIZE;
			mapHeight = (int) map.getHeight() * MapRender.BLOCK_SIZE;
		} catch (IOException e) { e.printStackTrace(); }
		//endregion

		//region Adds score sprites
		try {
			scoreSprite.put(-1, new Image(new FileInputStream("res/numbers/vs.png")));
			scoreSprite.put(0, new Image(new FileInputStream("res/numbers/0.png")));
			scoreSprite.put(1, new Image(new FileInputStream("res/numbers/1.png")));
			scoreSprite.put(2, new Image(new FileInputStream("res/numbers/2.png")));
			scoreSprite.put(3, new Image(new FileInputStream("res/numbers/3.png")));
		} catch (FileNotFoundException e) { e.printStackTrace(); }
		//endregion

		//region Pause menu
		root.setOnKeyPressed(keyEvent -> {
			if (keyEvent.getCode() == KeyCode.ESCAPE) {
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
					hasFinished = true;
					scene.setCursor(Cursor.DEFAULT);
					Client.disconnect();
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
			}
		});
		//endregion
	}

	public void updateEnemies() {
		Client.receivePositions();

		//region Replaces Bots <-> Players
		ArrayList<String> playerKeys = new ArrayList<>(playerList.keySet());

		for (String player : playerKeys) {
//            If bot name/player IP is stored locally - continue
			if (Client.listOfClients.connectedIPs.contains(player))
				continue;

//            If server list has been updated - reset the odd player's position and velocity
			playerList.get(player).reset();
			playerList.get(player).resetVelocity();

//            Find the odd player (bot or disconnected player)
			for (String newIP : Client.listOfClients.connectedIPs) {
//                If the player is in both local and server lists - continue
				if (playerList.containsKey(newIP))
					continue;

//                If newIP is in local list but not in the server list
				playerList.get(player).setIpOfClient(newIP); //Change the Player gameObject's IP to newIP
				playerList.put(newIP, playerList.get(player)); //Put a copy of the old player (with changed IP) as a
				// new entry
				playerList.remove(player); //Delete the old player entry from local list
			}
		}
		//endregion

		//region Updates info of *other* players/bots and the ball
		for (String ip : Client.listOfClients.posList.keySet()) {
			Player enemy = playerList.get(ip);
			if (ip.equals(thisPlayer.getIpOfClient()))
				continue;

			PositionPacket theDoubleValues = Client.listOfClients.posList.get(ip);

			//region Rotation / Position / Velocity
			if (!ip.equals("ball")) {
				Affine rotate = new Affine();
				rotate.appendRotation(theDoubleValues.degrees,
						theDoubleValues.posX - Camera.GetOffsetX() + thisPlayer.getWidth() / 2,
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
			Vector2 shotVel       = new Vector2(bulletX, bulletY).multiply(MOVEMENT_SPEED * 2);
			Vector2 gunDirection  = new Vector2(bulletX * 32, bulletY * 32);
			Vector2 bulletPos = new Vector2(theDoubleValues.posX + thisPlayer.getHeight() / 2,
					theDoubleValues.posY + thisPlayer.getWidth() / 2).add(gunDirection);

			editObjectManager(0, 0, bulletPos, shotVel, "enemy");
			theDoubleValues.bulletShot = false;
			//endregion
		}
		//endregion
	}

	private synchronized void editObjectManager(int op, double time, Vector2 bp, Vector2 bv, String shooter) {
		switch (op) {
			case 0: { //add bullets
				new Bullet(bp, bv, shooter);
				break;
			} //Add bullets

			case 1: { //remove bullets if needed
				HashSet<GameObject> crash_bullet_list = new HashSet<>();
				for (GameObject bullet : ObjectManager.getGameObjects()) {
					if (!bullet.getKeys().contains("Bullet"))
						continue;

					for (Bricks block : MapRender.GetList())
						if (bullet.getBounds().intersects(block.getBounds().getBoundsInLocal()))
							crash_bullet_list.add(bullet);

					for (String ip : Client.listOfClients.connectedIPs) {
						if (((Bullet) bullet).getShooterName().equals(ip))
							continue;

						Player player = playerList.get(ip);
						if (bullet.getBounds().intersects(player.circle.getBoundsInLocal())) {
							crash_bullet_list.add(bullet);
							//Players hit
							if (player.stunned == 0) {
								player.stunned = STUN_DURATION * 5;
								player.setVelocity(bullet.getVelocity().divide(3));
								player.setDeceleration(new Vector2(player.getVelocity()).divide(STUN_DURATION));
							}
						}
					}
				}

				for (GameObject bullet : crash_bullet_list)
					bullet.destroy();
				break;
			} //Remove bullets if needed

			case 2: { //update object positions
				for (IUpdateable go : ObjectManager.getGameObjects()) {
					if (go instanceof Ball) {
						if (((Ball) go).containsKey("ServerBall")) {
							continue;
						}
					} else if (go instanceof AIPlayer)
						continue;

					go.update(time);
				}

				break;
			} //Update object positions
		}
	}

	private void scored(char scoringSide) {
		if (scoringSide == side) {
			yourScore++;
			System.out.println("Goal for YOUR team!");
		} else {
			enemyScore++;
			System.out.println("Goal for the ENEMY team!");
		}

		thisPlayer.reset();
		ball.reset();

		try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
	}

	private static Connection getConnection() {
		Connection c = null;
		try {
			Class.forName("org.postgresql.Driver");
			String url = "jdbc:postgresql://rogue.db.elephantsql.com:5432/nuzmlzpr";
			url = url.trim();
			c   = DriverManager.getConnection(url, "nuzmlzpr", "pd7OdC_3BiVrAPNU68CETtFtBaqFxJFB");

			if (c != null) {
				System.out.println("Connection complete");
			} else {
				System.out.println("Connection failed");
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		return c;
	}

	private void closeConnections(Connection c, PreparedStatement p, ResultSet r) {
		if (c != null) {
			try {
				c.close();
			} catch (SQLException ignored) {}
		}
		if (p != null) {
			try {
				p.close();
			} catch (SQLException ignored) {}
		}
		if (r != null) {
			try {
				r.close();
			} catch (SQLException ignored) {}
		}
	}

	private void updateLBoard(Connection c, String username) {
		PreparedStatement preparedStatement = null;
		int               score             = currentScore(getConnection(), username);
		try {
			String query = "UPDATE userdatascore SET score = " + (++score) + " WHERE name = '" + username + "'";
			preparedStatement = c.prepareStatement(query);

			int rowAffected = preparedStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeConnections(c, preparedStatement, null);
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
			closeConnections(c, preparedStatement, rs);
		}
		return 0;
	}
}