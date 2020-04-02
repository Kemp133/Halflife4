package com.halflife3.Controller.GameModes;

import com.halflife3.Controller.*;
//import com.halflife3.Controller.Interfaces.ITimeLimit;
import com.halflife3.GameUI.AudioForGame;
import com.halflife3.Mechanics.GameObjects.*;
import com.halflife3.Mechanics.Interfaces.IRenderable;
import com.halflife3.Mechanics.Interfaces.IUpdateable;
import com.halflife3.Mechanics.Vector2;
import com.halflife3.Networking.Client.Client;
import com.halflife3.Networking.Packets.PositionPacket;
import com.halflife3.Networking.Server.Server;
import com.halflife3.View.Camera;
import com.halflife3.View.MapRender;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.stage.*;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import static javafx.scene.input.KeyCode.*;
import static javafx.scene.input.KeyCode.S;

public class MainMode extends GameMode /*implements ITimeLimit*/ {
//	protected Timer  timer; //Not using timer at the minute
	protected double scoreLimit;
	protected HashMap<Integer, Double> scores;
	protected HashMap<Integer, String> teams;

	private         final int   FPS                 = 30;
	private         final int   GAME_WINDOW_HEIGHT  = 600;
	private         final int   GAME_WINDOW_WIDTH   = 800;
	private         final int   MOVEMENT_SPEED      = 120;
	public static   final int   SHOT_SPEED          = 200;
	public static   final float STUN_DURATION       = 20;

	//region Other variables
	private static Pane root;
	private static Player thisPlayer;
	private static HashMap<String, Player> playerList;
	private static HashMap<Integer, Image> scoreSprite;
	private Input input = Input.getInstance();
	private static ProgressBar[] stunBar;
	private static Ball ball;
	private Stage window = null;
	private char side;
	private int yourScore = 0;
	private int enemyScore = 0;
	private boolean flag = false;
	public boolean running = false;
	private int bulletLimiter = 0;
	public int mapWidth;
	private int mapHeight;
	//    private int END_SCENE_DURATION = 300;
	private final int RIGHT_END_OF_SCREEN = 11*40;
	private final int LEFT_END_OF_SCREEN = 9*40;
	private final int BOTTOM_OF_SCREEN = 8*40;
	private final int TOP_OF_SCREEN = 7*40;
	private GraphicsContext graphicsContext;
	//endregion

//	public MainMode() {}


	{
//		timer = new Timer(5 * Timer.MINUTE, Timer.TimeDirection.DOWN);
		teams = new HashMap<>();
	}

	public MainMode (String GameModeName, double score) {
		super(GameModeName);
//		this.CanRespawn = CanRespawn;
		scoreLimit = score;
	}

	@Override
	void initialise () {
		//region Networking
		Client clientNetwork = new Client();
		clientNetwork.joinGroup();
		clientNetwork.getHostInfo();
		clientNetwork.start();
		//endregion

		//region Initialise Objects
		playerList = new HashMap<>();
		scoreSprite = new HashMap<>();
		stunBar = new ProgressBar[4];
		root = new Pane();
		//endregion

		//region Initialise This Player
		thisPlayer = new Player(clientNetwork.getStartingPosition(), new Vector2(0,0));
		thisPlayer.setIpOfClient(clientNetwork.getClientAddress().toString());
		//endregion

		//region Wait For Server To Acknowledge Player Connection
		do {
			Client.receivePositions();
		} while(!Client.listOfClients.connectedIPs.contains(thisPlayer.getIpOfClient()));
		//endregion

		//region Initialise The Players With Player Controlled and AI Controlled
		initialisePlayers();
		//endregion

		//region Setting Up Scene
		Canvas canvas = new Canvas(GAME_WINDOW_WIDTH, GAME_WINDOW_HEIGHT);
		root.getChildren().add(canvas);
		graphicsContext = canvas.getGraphicsContext2D();

		Scene scene = new Scene(root, GAME_WINDOW_WIDTH, GAME_WINDOW_HEIGHT);
		SceneManager.getInstance().setScene("Game Scene", scene);
		//endregion

		//region Initialise Game Objects
		gameInit(scene);
		//endregion

		//region Setting Side Of Players
		side = (thisPlayer.getPosX() < mapWidth / 2f) ? 'L' : 'R';
		//endregion

		//region Initialising Stun Bars
		for (int i = 0; i < Server.startPositions.length; i++) {
			stunBar[i] = new ProgressBar(0);
			stunBar[i].setStyle("-fx-accent: green;");
			stunBar[i].setPrefHeight(8);
			stunBar[i].setPrefWidth(40);
			root.getChildren().add(stunBar[i]);
		}
		//endregion

		//region Initialise Ball
		ball = new Ball(new Vector2(mapWidth / 2f, mapHeight / 2f), new Vector2(0,0));
		//endregion

		//region Thread To Update Position Of All Enemies and The Ball
		running = true;
		new Thread(() -> { while(running) updateEnemies(); }).start();
		//endregion

		System.out.println("Game Running");
	}

	@Override
	void gameLoop () {
		new AnimationTimer() {
			long lastUpdate = System.nanoTime();
			double elapsedTime = 0;
//            int fpsCounter = 0;
//            double second = 1;

			public void handle(long currentNanoTime) {
				if (currentNanoTime - lastUpdate < 1e9/(FPS * 1.07))
					return;

				//region Calculate time since last update.
				elapsedTime = (currentNanoTime - lastUpdate) / 1e9;
				lastUpdate = currentNanoTime;
				//endregion

				//region Camera offset
				Camera.SetOffsetX(thisPlayer.getPosX() - LEFT_END_OF_SCREEN);
				Camera.SetOffsetY(thisPlayer.getPosY() - TOP_OF_SCREEN);
				if (Camera.GetOffsetX() < 0)
					Camera.SetOffsetX(0);
				else if (Camera.GetOffsetX() > mapWidth - LEFT_END_OF_SCREEN - RIGHT_END_OF_SCREEN)
					Camera.SetOffsetX(mapWidth - LEFT_END_OF_SCREEN - RIGHT_END_OF_SCREEN);
				if (Camera.GetOffsetY() < 0)
					Camera.SetOffsetY(0);
				else if (Camera.GetOffsetY() > mapHeight - TOP_OF_SCREEN - BOTTOM_OF_SCREEN)
					Camera.SetOffsetY(mapHeight - TOP_OF_SCREEN - BOTTOM_OF_SCREEN);
				//endregion

				//region Calculate the rotation
				Vector2 playerClientCenter =
						new Vector2(thisPlayer.getPosX() - Camera.GetOffsetX() + thisPlayer.getWidth() / 2,
								thisPlayer.getPosY() - Camera.GetOffsetY() + thisPlayer.getHeight() / 2);
				Vector2 direction =
						new Vector2(input.getMousePosition().getX(), input.getMousePosition().getY())
								.subtract(playerClientCenter);

				Affine rotate = new Affine();
				short deg = (short) Math.toDegrees(Math.atan2(direction.getY(), direction.getX()));
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
//                Player collision
				for (Bricks block : MapRender.GetList())
					if (block.getBounds().intersects(thisPlayer.circle.getBoundsInLocal()))
						thisPlayer.collision(block, elapsedTime);

//                Bullet collision
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
					double bulletX = Math.cos(Math.atan2(direction.getY(), direction.getX()));
					double bulletY = Math.sin(Math.atan2(direction.getY(), direction.getX()));
					Vector2 shotVelocity = new Vector2(bulletX, bulletY).multiply(SHOT_SPEED);

					if (thisPlayer.isHoldingBall()) { // Shoots the ball
						boolean ballInWall = false;
						for (Bricks block : MapRender.GetList())
							if (ball.getBounds().intersects(block.getBounds().getBoundsInLocal())) {
								ballInWall = true;
								break;
							}

						thisPlayer.setBulletShot(!ballInWall);
					} else { // Shoots a bullet
						Vector2 gunDirection = new Vector2(bulletX * 32, bulletY * 32);
						Vector2 bulletPos = new Vector2(thisPlayer.getPosX() + thisPlayer.getHeight() / 2,
								thisPlayer.getPosY() + thisPlayer.getWidth() / 2).add(gunDirection);

						editObjectManager
								(0, 0, bulletPos, shotVelocity, thisPlayer.getIpOfClient());
						thisPlayer.setBulletShot(true);
					}
					bulletLimiter = FPS / 5;
				} else if (bulletLimiter > 0) bulletLimiter--;
				//endregion

				//region Re-renders all game objects
				graphicsContext.clearRect(0, 0, GAME_WINDOW_WIDTH, GAME_WINDOW_HEIGHT);

				MapRender.Render(graphicsContext);

				for (IRenderable go : ObjectManager.getGameObjects())
					go.render(graphicsContext);
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

				//region Sends the client's position, whether they've shot a bullet and if they're holding the ball
				if (thisPlayer.stunned != 0) thisPlayer.setHoldsBall(false);
				Client.sendPacket(thisPlayer.getPacketToSend(), Client.getUniquePort());
				if (thisPlayer.bulletShot) thisPlayer.setHoldsBall(false);
				//endregion

				//region Checks if a goal has been scored
				if (ball.getPosX() < Server.GOAL_WIDTH)
					scored('R', elapsedTime, graphicsContext);
				else if (ball.getPosX() > mapWidth - Server.GOAL_WIDTH)
					scored('L', elapsedTime, graphicsContext);
				//endregion

				//region Renders the score text
				graphicsContext.drawImage(scoreSprite.get(yourScore), GAME_WINDOW_WIDTH / 2f - 40, 40);
				graphicsContext.drawImage(scoreSprite.get(-1), GAME_WINDOW_WIDTH / 2f + 10, 40);
				graphicsContext.drawImage(scoreSprite.get(enemyScore), GAME_WINDOW_WIDTH / 2f + 40, 40);
				//endregion
			}
		}.start();

		root.requestFocus();
	}

	@Override
	void finished () {
		//Send packet to end the game
		//Set up game to await for the won/lost packet
		System.out.println("Game exited");
		running = false;
		Client.disconnect();
		SceneManager.getInstance().restorePreviousScene(); //End the flow of this class, return to the previous scene
	}

	@Override
	boolean won (int team) {
		//Send team with id of @team the won packet
		//Send everybody else the lost packet

		return false;
	}

	@Override
	boolean lost () {
		return false;
	}

//	@Override
//	public boolean timeLimitReached (double delta) {
//		return timer.getHasFinished();
//	}

	/** A method to initialise the players in the game */
	public void initialisePlayers() {
		Client.receivePositions();
		for (String ip : Client.listOfClients.connectedIPs) {
			if (ip.equals(thisPlayer.getIpOfClient())) {
				playerList.put(ip, thisPlayer);
				continue;
			}

			PositionPacket theDoubleValues = Client.listOfClients.posList.get(ip);
			Vector2        pos             = new Vector2(theDoubleValues.posX, theDoubleValues.posY);
			Vector2 vel = new Vector2(theDoubleValues.velX, theDoubleValues.velY);
			Player enemy = new Player(pos, vel);
			enemy.setIpOfClient(ip);
			playerList.put(ip, enemy);
		}
	}

	private void gameInit(Scene scene) {
		//region Background setup
		try {
			Image image = new Image(new FileInputStream("res/Space.png"));
			var bgSize = new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO,
					false, false, true, true);
			var myBI = new BackgroundImage(image, null, null, null, bgSize);
			root.setBackground(new Background(myBI));
		} catch (FileNotFoundException e) {
			System.out.println("Could not find file in path: 'res/Space.png'");
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
		audio.getMute().setOnAction(actionEvent -> audio.swtichMute());
		audio.getSlider1().setOnAction(actionEvent -> audio.volumeControl(audio.getVolume()));
		//root.getChildren().add(audio.getMenuBar());
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
			BufferedImage map = ImageIO.read(new File("res/map.png"));
			mapWidth = map.getWidth() * 40;
			mapHeight = map.getHeight() * 40;
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
				returnToGame.setOnAction(event-> {
					root.setEffect(null);
					popupStage.hide();
				});

				//region Audio button
				Button sound = new Button("Audio On/Off");
				pauseRoot.getChildren().add(sound);
				sound.setOnAction(actionEvent -> audio.swtichMute());
				//endregion

				//region ToMainMenu button
				Button toMainMenu = new Button("Quit to Main Menu");
				pauseRoot.getChildren().add(toMainMenu);
				toMainMenu.setOnAction(actionEvent -> {

				});
				//endregion

				//region ToDesktop button
				Button toDesktop = new Button("Quit to Desktop");
				pauseRoot.getChildren().add(toDesktop);
				toDesktop.setOnAction(actionEvent -> Platform.exit());
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
				playerList.put(newIP, playerList.get(player)); //Put a copy of the old player (with changed IP) as a new entry
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

				if (ball.getPosX() < Server.GOAL_WIDTH || ball.getPosX() > mapWidth - Server.GOAL_WIDTH) {
					try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
					return;
				}

				continue;
			}
			//endregion

			//region Enemies' bullet shots
			if (!theDoubleValues.bulletShot)
				continue;

			double degreeRadians = Math.toRadians(theDoubleValues.degrees);
			double bulletX = Math.cos(degreeRadians);
			double bulletY = Math.sin(degreeRadians);
			Vector2 shotVel = new Vector2(bulletX, bulletY).multiply(MOVEMENT_SPEED * 2);
			Vector2 gunDirection = new Vector2(bulletX * 32, bulletY * 32);
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
			case 0 : { //add bullets
				new Bullet(bp, bv, shooter);
				break;
			} //Add bullets

			case 1 : { //remove bullets if needed
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
								player.setMoving(false);
							}
						}
					}
				}

				for (GameObject bullet : crash_bullet_list)
					bullet.destroy();
				break;
			} //Remove bullets if needed

			case 2 : { //update object positions
				for (IUpdateable go : ObjectManager.getGameObjects())
					go.update(time);
				break;
			} //Update object positions
		}
	}

	private void scored(char scoringSide, double time, GraphicsContext gc) {
		if (scoringSide == side) {
			yourScore++;
			System.out.println("Goal for YOUR team!");
		} else {
			enemyScore++;
			System.out.println("Goal for the ENEMY team!");
		}

		thisPlayer.reset();
		ball.reset();

		try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
	}

	//region Get/Set Scores
	public double getTeamScore (int team) { return scores.get(team); }

	public void setTeamScore (int team, double score) { this.scores.put(team, score); }
	//endregion
}
