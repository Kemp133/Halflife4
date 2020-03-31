package com.halflife3.Networking.Client;

import com.halflife3.Controller.Input;
import com.halflife3.Controller.KeyboardInput;
import com.halflife3.Controller.MouseInput;
import com.halflife3.Controller.ObjectManager;
import com.halflife3.GameUI.AudioForGame;
import com.halflife3.Mechanics.GameObjects.*;
import com.halflife3.Mechanics.Interfaces.IRenderable;
import com.halflife3.Mechanics.Interfaces.IUpdateable;
import com.halflife3.Mechanics.Vector2;
import com.halflife3.Networking.Packets.PositionPacket;
import com.halflife3.View.Camera;
import com.halflife3.View.MapRender;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static com.halflife3.Networking.Client.Client.listOfClients;
import static javafx.scene.input.KeyCode.*;

public class ClientGame extends Application {
    private         final int   FPS                 = 30;
    private         final int   GAME_WINDOW_HEIGHT  = 600;
    private         final int   GAME_WINDOW_WIDTH   = 800;
    private         final int   MOVEMENT_SPEED      = 120;
    public static   final int   SHOT_SPEED          = 200;
    public static   final float STUN_DURATION       = 100;

    //region Other variables
    static Input input;
    private static Pane root;
    private static Player thisPlayer;
    private static HashMap<String, Player> playerList;
    private static ProgressBar[] stunBar;
    private static BasicBall ball;
    private Stage window = null;
    private char side;
    private boolean goal = false;
    private double ballPreviousX;
    private int goal_width = 4;
    private int your_score = 0;
    private int enemy_score = 0;
    private boolean win;
    private static HashMap<Integer, Image> score_sprite;

    private boolean flag = false;
    public boolean running = false;
    private int bulletLimiter = 0;
    public int mapWidth;
    private int mapHeight;
    private final int RIGHT_END_OF_SCREEN = 11*40;
    private final int LEFT_END_OF_SCREEN = 9*40;
    private final int BOTTOM_OF_SCREEN = 8*40;
    private final int TOP_OF_SCREEN = 7*40;
    //endregion

    //region ClientGame constructors
    public ClientGame() {}

    public ClientGame(Stage window) {
        this.window = window;
        flag = true;
    }
    //endregion

    public void getStarted() {
        //region Initialise network
        Client clientNetwork = new Client();
        clientNetwork.joinGroup();
        clientNetwork.getHostInfo();
        clientNetwork.start();
        //endregion

        //region Initialise objects
        playerList = new HashMap<>();
        score_sprite = new HashMap<>();
        stunBar = new ProgressBar[4];
        input = new Input();
        root = new Pane();
        //endregion

        //region Initialise this player
        Vector2 startPos = clientNetwork.getStartingPosition();
        Vector2 startVel = new Vector2(0, 0);
        thisPlayer = new Player(startPos, startVel);
        thisPlayer.setIpOfClient(clientNetwork.getClientAddress().toString());
        thisPlayer.setAI(false);
        //endregion

        //region Wait until Server acknowledges Player connection
        do {
            Client.receivePositions();
        } while (!listOfClients.connectedIPs.contains(thisPlayer.getIpOfClient()));
        //endregion

        initialisePlayers();

        if (flag) {
            flag = false;
            try { this.start(this.window); } catch (Exception e) { e.printStackTrace(); }
        } else launch();
    }

    @Override
    public void start(Stage primaryStage) {
        //region Window setup
        primaryStage.setTitle("HalfLife 3 : Man in Black");
        Canvas canvas = new Canvas(GAME_WINDOW_WIDTH, GAME_WINDOW_HEIGHT);
        root.getChildren().add(canvas);
        Scene scene = new Scene(root, GAME_WINDOW_WIDTH, GAME_WINDOW_HEIGHT);
        primaryStage.setScene(scene);
        GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
        gameInit(scene);// << BG, cursor, audio, key input, map loading
        side = (thisPlayer.getPosX() < mapWidth / 2f) ? 'L' : 'R'; // Sets the team of this player
        //endregion

        //region Initialise stun bars
        for (int i = 0; i < 4; i++) {
            stunBar[i] = new ProgressBar(0);
            stunBar[i].setStyle("-fx-accent: green;");
            stunBar[i].setPrefHeight(8);
            stunBar[i].setPrefWidth(40);
            root.getChildren().add(stunBar[i]);
        }
        //endregion

        //region Initialise ball
        ball = new BasicBall(new Vector2(mapWidth / 2f, mapHeight / 2f), new Vector2(0, 0));
        ballPreviousX = mapWidth / 2f;
        //endregion

        //region Thread to update the position of all enemies and the ball
        running = true;
        new Thread(() -> { while (running) updateEnemies(); }).start();
        //endregion

        System.out.println("Game running");

        new AnimationTimer() {
            long lastUpdate = System.nanoTime();
//            int fpsCounter = 0;
//            double second = 1;

            public void handle(long currentNanoTime) {
                if (currentNanoTime - lastUpdate < 1e9/(FPS * 1.07))
                    return;

                //region Calculate time since last update.
                double elapsedTime = (currentNanoTime - lastUpdate) / 1e9;
                lastUpdate = currentNanoTime;
                //endregion

                //TODO: Check the goal status

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
                        new Vector2(Input.mousePosition.getX(), Input.mousePosition.getY())
                                .subtract(playerClientCenter);

                Affine rotate = new Affine();
                short deg = (short) Math.toDegrees(Math.atan2(direction.getY(), direction.getX()));
                rotate.appendRotation(deg, playerClientCenter.getX(), playerClientCenter.getY());
                thisPlayer.setDegrees(deg);
                thisPlayer.setAffine(rotate);
                //endregion

                //region Handles player movement
                if (thisPlayer.stand == 0) {
                    if (Input.isKeyReleased(A) && Input.isKeyReleased(D)) {
                        thisPlayer.getVelocity().setX(0);
                    }
                    if (Input.isKeyReleased(W) && Input.isKeyReleased(S)) {
                        thisPlayer.getVelocity().setY(0);
                    }
                    if (Input.isKeyPressed(A)) {
                        thisPlayer.getVelocity().setX(-MOVEMENT_SPEED);
                    }
                    if (Input.isKeyPressed(D)) {
                        thisPlayer.getVelocity().setX(MOVEMENT_SPEED);
                    }
                    if (Input.isKeyPressed(W)) {
                        thisPlayer.getVelocity().setY(-MOVEMENT_SPEED);
                    }
                    if (Input.isKeyPressed(S)) {
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
                var playerCenter = new Vector2(thisPlayer.circle.getCenterX(), thisPlayer.circle.getCenterY());
                var ballPos = new Vector2(ball.getPosition());
                var nextBallPos = new Vector2(ballPos).add(new Vector2(ball.getVelocity()).multiply(elapsedTime));
                boolean playerIsTouchingTheBall = ball.getBounds().intersects(thisPlayer.circle.getBoundsInLocal());

                if(playerIsTouchingTheBall){ thisPlayer.setHoldsBall(true);}
                //endregion

                //region Shoots a bullet or the ball
                thisPlayer.setBulletShot(false);
                if (Input.mouseButtonPressed.get(MouseButton.PRIMARY) && bulletLimiter == 0) {
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

                        if (!ballInWall) {
//                           ball.setVelocity(new Vector2(shotVelocity).multiply(1.5));
//                           ball.setAcceleration(new Vector2(shotVelocity).divide(100));
                            thisPlayer.setBulletShot(true);
                        }
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
                    stunBar[id].setProgress(playerList.get(ip).stand / STUN_DURATION);
                    id++;
                }
                //endregion

                //region Sends the client's position, whether they've shot a bullet and if they're holding the ball
                Client.sendPacket(thisPlayer.getPacketToSend(), Client.getUniquePort());
                if (thisPlayer.bulletShot) thisPlayer.setHoldsBall(false);
                //endregion

                //region Checks if a goal has been scored
                if ((side == 'L' && ballPreviousX - ball.getPosX() > mapWidth / 4f) ||
                        (side == 'R' && ballPreviousX - ball.getPosX() < -mapWidth / 4f)) {
                    your_score++;
                    goal = true;
                    System.out.println("Goal for YOUR team!");
                }
                if ((side == 'R' && ballPreviousX - ball.getPosX() > mapWidth / 4f) ||
                        (side=='L' && ballPreviousX - ball.getPosX() < -mapWidth / 4f)){
                    enemy_score++;
                    System.out.println("Goal for the ENEMY team!");
                    goal = true;
                }
                ballPreviousX = ball.getPosX();
                //endregion

                //region Renders the score text
                graphicsContext.drawImage(score_sprite.get(your_score), GAME_WINDOW_WIDTH / 2f - 40, 40);
                graphicsContext.drawImage(score_sprite.get(-1), GAME_WINDOW_WIDTH / 2f + 10, 40);
                graphicsContext.drawImage(score_sprite.get(enemy_score), GAME_WINDOW_WIDTH / 2f + 40, 40);
                //endregion

                //TODO: If ESC is pressed -> Open Pause Menu

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
        }.start();

        root.requestFocus();
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        System.out.println("Game exited");
        running = false;
        Client.disconnect();
        super.stop();
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
        new Crosshair(Input.mousePosition, new Vector2(0, 0));
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
            score_sprite.put(-1, new Image(new FileInputStream("res/numbers/vs.png")));
            score_sprite.put(0, new Image(new FileInputStream("res/numbers/0.png")));
            score_sprite.put(1, new Image(new FileInputStream("res/numbers/1.png")));
            score_sprite.put(2, new Image(new FileInputStream("res/numbers/2.png")));
            score_sprite.put(3, new Image(new FileInputStream("res/numbers/3.png")));
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
                //endregion

                //region Options button
//                Button options = new Button("Options");
//                pauseRoot.getChildren().add(options);
//                options.setOnAction(actionEvent -> {
//
//                });
                //endregion

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
                toDesktop.setOnAction(actionEvent -> {

                });
                //endregion

                popupStage.show();
            }
        });
        //endregion
    }

    public void initialisePlayers() {
        Client.receivePositions();
        for (String ip : listOfClients.connectedIPs) {
            if (ip.equals(thisPlayer.getIpOfClient())) {
                playerList.put(ip, thisPlayer);
                continue;
            }

            PositionPacket theDoubleValues = listOfClients.posList.get(ip);
            Vector2 pos = new Vector2(theDoubleValues.posX, theDoubleValues.posY);
            Vector2 vel = new Vector2(theDoubleValues.velX, theDoubleValues.velY);
            Player enemy = new Player(pos, vel);
            enemy.setIpOfClient(ip);
            playerList.put(ip, enemy);
        }
    }

    public void updateEnemies() {
        Client.receivePositions();

        //region Replaces Bots <-> Players
        ArrayList<String> playerKeys = new ArrayList<>(playerList.keySet());

        for (String player : playerKeys) {
//            If bot name/player IP is stored locally - continue
            if (listOfClients.connectedIPs.contains(player))
                continue;

//            If server list has been updated - reset the odd player's position and velocity
            playerList.get(player).resetPosition();
            playerList.get(player).resetVelocity();

//            Find the odd player (bot or disconnected player)
            for (String newIP : listOfClients.connectedIPs) {
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
        for (String ip : listOfClients.posList.keySet()) {
            Player enemy = playerList.get(ip);
            if (ip.equals(thisPlayer.getIpOfClient()))
                continue;

            PositionPacket theDoubleValues = listOfClients.posList.get(ip);

            //region Rotation / Position / Velocity
            if (!ip.equals("ball")) {
                Affine rotate = new Affine();
                rotate.appendRotation(theDoubleValues.degrees,
                        theDoubleValues.posX - Camera.GetOffsetX() + thisPlayer.getWidth() / 2,
                        theDoubleValues.posY - Camera.GetOffsetY() + thisPlayer.getHeight() / 2);

                enemy.setAffine(rotate);
                enemy.setVelocity(theDoubleValues.velX, theDoubleValues.velY);
                enemy.setPosition(theDoubleValues.posX, theDoubleValues.posY);
            } else {
                ball.setVelocity(theDoubleValues.velX, theDoubleValues.velY);
                ball.setPosition(theDoubleValues.posX, theDoubleValues.posY);
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

                    for (String ip : listOfClients.connectedIPs) {
                        if (((Bullet) bullet).getShooterName().equals(ip))
                            continue;

                        Player player = playerList.get(ip);
                        if (bullet.getBounds().intersects(player.circle.getBoundsInLocal())) {
                            crash_bullet_list.add(bullet);
                            //Players hit
                            if (player.stand == 0) {
                                player.stand = STUN_DURATION;
                                player.setVelocity(bullet.getVelocity());
                                player.setAcceleration(new Vector2(player.getVelocity()).divide(42));
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
}