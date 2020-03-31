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
import javafx.event.EventHandler;
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
    public static   final int   SHOT_SPEED          = 240;
    public static   final float STUN_DURATION       = 100;

    //region Other variables
    static Input input;
    private static Pane root;
    private static Player thisPlayer;
    private static HashMap<String, Player> playerList;
    private static ProgressBar[] stunBar;
    private static BasicBall ball;
    private Stage window = null;


    //L for left and R for Right
    private char side;
    //The width of the goal area
    private boolean goal = false;
    private double original_ball_x;
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
        //Set the team of this player
        if(thisPlayer.getPosX()<mapWidth/2)
            side = 'L';
        else
            side = 'R';
        System.out.println(side);
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
        //endregion

        //region Thread to update the position of all enemies and the ball
        running = true;
        new Thread(() -> {
            while (running) {
                updateEnemies();
            }
        }).start();
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
                Vector2 player_client_center =
                        new Vector2(thisPlayer.getPosX() - Camera.GetOffsetX() + thisPlayer.getWidth() / 2,
                                thisPlayer.getPosY() - Camera.GetOffsetY() + thisPlayer.getHeight() / 2);
                Vector2 direction =
                        new Vector2(Input.mousePosition.getX(), Input.mousePosition.getY())
                                .subtract(player_client_center);

                Affine rotate = new Affine();
                short deg = (short) Math.toDegrees(Math.atan2(direction.getY(), direction.getX()));
                rotate.appendRotation(deg, player_client_center.getX(), player_client_center.getY());
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

//                Ball collision
//                ballWallBounce();
                //endregion

                //region Updates position of all game objects locally (has to go after collision)
                editObjectManager(2, elapsedTime, null, null, null);
                //endregion

                //region Checks if the player is holding the ball
                //TODO: don't need to check let the server deal with it
                var playerCenter = new Vector2(thisPlayer.circle.getCenterX(), thisPlayer.circle.getCenterY());
                var ballPos = new Vector2(ball.getPosition());
                var nextBallPos = new Vector2(ballPos).add(new Vector2(ball.getVelocity()).multiply(elapsedTime));
                boolean playerIsTouchingTheBall = ball.getBounds().intersects(thisPlayer.circle.getBoundsInLocal());
                boolean ballMovingAway = playerCenter.distance(ballPos) < playerCenter.distance(nextBallPos);

                if(playerIsTouchingTheBall){
                    thisPlayer.setHoldsBall(true/* && !ballMovingAway*/);
                }
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
                            ball.setVelocity(new Vector2(shotVelocity).multiply(1.5));
                            ball.setAcceleration(new Vector2(shotVelocity).divide(100));
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
                if(thisPlayer.bulletShot){
                    thisPlayer.setHoldsBall(false);
                }
                //endregion

                //Check if goal

                //TODO:
                if((side == 'L' && original_ball_x - ball.getPosX() > mapWidth/4) ||(side=='R' && original_ball_x - ball.getPosX() < -mapWidth/4)) {
                    your_score++;
                    goal = true;
                    System.out.println("goal");
                }
                if((side == 'R' && original_ball_x - ball.getPosX() > mapWidth/4) ||(side=='L' && original_ball_x - ball.getPosX() < -mapWidth/4)){
                    enemy_score++;
                    System.out.println("goal");
                    goal = true;
                }
                original_ball_x = ball.getPosX();
                //Show the score
                graphicsContext.drawImage(score_sprite.get(your_score), GAME_WINDOW_WIDTH/2-40, 40);
                graphicsContext.drawImage(score_sprite.get(-1), GAME_WINDOW_WIDTH/2 + 10, 40);
                graphicsContext.drawImage(score_sprite.get(enemy_score), GAME_WINDOW_WIDTH/2+40, 40);


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

        //if esc is pressed --> enter pause menu

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
        FileInputStream bgPNG;
        try {
            bgPNG = new FileInputStream("res/background_image.png");
            Image image = new Image(bgPNG, 40, 40, true, true);
            BackgroundImage myBI = new BackgroundImage(image,
                    BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.DEFAULT,
                    BackgroundSize.DEFAULT);
            root.setBackground(new Background(myBI));
        } catch (FileNotFoundException e) {
            System.out.println("Could not find file in path: 'res/background_image.png'");
        }
        //endregion

        //region Initialise cursor
        new Crosshair(Input.mousePosition.subtract(new Vector2(14, 14)), new Vector2(0, 0));
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
            original_ball_x = mapWidth/2;
            Image zero = new Image(new FileInputStream("res/numbers/0.png"));
            score_sprite.put(0,zero);
            Image one = new Image(new FileInputStream("res/numbers/1.png"));
            score_sprite.put(1,one);
            Image two = new Image(new FileInputStream("res/numbers/2.png"));
            score_sprite.put(2,two);
            Image three = new Image(new FileInputStream("res/numbers/3.png"));
            score_sprite.put(3,three);
            Image vs = new Image(new FileInputStream("res/numbers/vs.png"));
            score_sprite.put(-1,vs);
        } catch (IOException e) { e.printStackTrace(); }

        //the pause menu
        root.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if(keyEvent.getCode()== KeyCode.ESCAPE){
                    //set background effect
                    root.setEffect(new GaussianBlur());
                    //setting up vbox for buttons
                    //we will have 1 line of description with 4 buttons, that 5 rows
                    VBox pauseRoot = new VBox(6);
                    pauseRoot.setStyle("-fx-background-color: rgba(255, 255, 255, 0.8);");
                    pauseRoot.setAlignment(Pos.CENTER);
                    pauseRoot.setPadding(new Insets(20));
                    //actual stuff in the rows
                    pauseRoot.getChildren().add(new Label("Paused"));
                    //Button 1
                    Button returnToGame = new Button("Return to game");
                    pauseRoot.getChildren().add(returnToGame);
                    //Button 2
                    /*
                    Button options = new Button("Options");
                    pauseRoot.getChildren().add(options);
                    */
                    //button 2.5
                    Button sound = new Button("Audio On/Off");
                    pauseRoot.getChildren().add(sound);
                    //Button 3
                    Button toMainMenu = new Button("Quit to Main Menu");
                    pauseRoot.getChildren().add(toMainMenu);
                    //Button 4
                    Button toDesktop = new Button("Quit to Desktop");
                    pauseRoot.getChildren().add(toDesktop);
                    //actual stage of the pause menu
                    Stage popupStage = new Stage(StageStyle.TRANSPARENT);
                    popupStage.initOwner(window);
                    popupStage.initModality(Modality.APPLICATION_MODAL);
                    popupStage.setScene(new Scene(pauseRoot, Color.TRANSPARENT));
                    //Button function 1:resume
                    returnToGame.setOnAction(event-> {
                        root.setEffect(null);
                        popupStage.hide();
                    });
                    //Button function 2: options(audio)
                    sound.setOnAction(actionEvent -> {
                        audio.swtichMute();
                    });
                    toMainMenu.setOnAction(actionEvent -> {

                    });
                    toDesktop.setOnAction(actionEvent -> {
                    });
                    popupStage.show();
                }
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

    private void ballWallBounce() {
        for (Bricks block : MapRender.GetList()) {
            if (!ball.getBounds().intersects(block.getBounds().getBoundsInLocal()) || thisPlayer.isHoldingBall())
                continue;

            Vector2 brickCenter = new Vector2(block.getPosX() + block.getWidth() / 2,
                    block.getPosY() + block.getHeight() / 2);
            Vector2 ballCenter = new Vector2(ball.getPosX() + ball.getWidth() / 2,
                    ball.getPosY() + ball.getHeight() / 2);

            Vector2 relevantPos = new Vector2(ballCenter).subtract(brickCenter);
            double rel_x = relevantPos.getX();
            double rel_y = relevantPos.getY();

            if ((rel_x > 0 && rel_x < 38 && rel_y > -33 && rel_y < 33 && ball.getVelX() < 0) ||
                    rel_x > -38 && rel_x < 0 && rel_y > -33 && rel_y < 33 && ball.getVelX() > 0) {
                ball.collision(1);
            } else if ((rel_x > -33 && rel_x < 33 && rel_y > 0 && rel_y < 38 && ball.getVelY() < 0) ||
                    rel_x > -33 && rel_x < 33 && rel_y > -38 && rel_y < 0 && ball.getVelY() > 0) {
                ball.collision(2);
            }
        }
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