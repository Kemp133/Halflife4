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
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.transform.Affine;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import static com.halflife3.Networking.Client.Client.listOfClients;
import static javafx.scene.input.KeyCode.*;
import static javafx.scene.input.KeyCode.S;

public class ClientGame extends Application {
    private final int FPS = 30;
    private final int INC_PACKETS_PER_SECOND = 30;
    private final int GAME_WINDOW_HEIGHT = 600;
    private final int GAME_WINDOW_WIDTH = 800;
    private final int MOVEMENT_SPEED = 100;
    public static final float STUN_DURATION = 100;

    //region Other variables
    static Input input;
    private static Pane root;
    private static Player player_client;
    private static HashMap<String, Player> playerList;
    private static ProgressBar[] stunBar;
    private static BasicBall ball;
    private Stage window = null;
    private boolean flag = false;
    public boolean running = false;
    private int bulletLimiter = 5;
    private int mapWidth;
    private int mapHeight;
    private final int LEFT_END_OF_SCREEN = 9*40;
    private final int RIGHT_END_OF_SCREEN = 11*40;
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
        stunBar = new ProgressBar[4];
        input = new Input();
        root = new Pane();
        //endregion

        //region Initialise this player
        Vector2 startPos = clientNetwork.getStartingPosition();
        Vector2 startVel = new Vector2(0, 0);
        player_client = new Player(startPos, startVel);
        player_client.setIpOfClient(clientNetwork.getClientAddress().toString());
        player_client.setAI(false);
        //endregion

        //region Wait until Server acknowledges Player connection
        do {
            Client.receivePositions();
        } while (!listOfClients.connectedIPs.contains(player_client.getIpOfClient()));
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
//        ball = new BasicBall(new Vector2(mapWidth / 2f - 40, mapHeight / 2f - 40), new Vector2(0, 0));
        ball = new BasicBall(new Vector2(player_client.getPosX(), player_client.getPosY() - 80), new Vector2(0, 0));
        //endregion

        //region Thread to update the position of all enemies
        running = true;
        new Thread(() -> {
            double serverNanoTime = System.nanoTime();
            while (running) {
                if (System.nanoTime() - serverNanoTime > Math.round(1.0/ INC_PACKETS_PER_SECOND * 1e9)) {
                    updateEnemies();
                    serverNanoTime = System.nanoTime();
                }
            }
        }).start();
        //endregion

        System.out.println("Client running");

        new AnimationTimer() {
            private long lastUpdate = 0;
            double startNanoTime = System.nanoTime();
            boolean ballShot = false;

            public void handle(long currentNanoTime) {
                if (currentNanoTime - lastUpdate < Math.round(1e9/FPS))
                    return;

                //region Calculate time since last update.
                double elapsedTime = (currentNanoTime - startNanoTime) / 1e9;
                startNanoTime = currentNanoTime;
                //endregion

                //region Camera offset
                Camera.SetOffsetX(player_client.getPosX() - LEFT_END_OF_SCREEN);
                Camera.SetOffsetY(player_client.getPosY() - TOP_OF_SCREEN);
                if (Camera.GetOffsetX() < 0)
                    Camera.SetOffsetX(0);
                else if (Camera.GetOffsetX() > mapWidth - LEFT_END_OF_SCREEN - RIGHT_END_OF_SCREEN)
                    Camera.SetOffsetX(mapWidth - LEFT_END_OF_SCREEN - RIGHT_END_OF_SCREEN);
                if (Camera.GetOffsetY() < 0)
                    Camera.SetOffsetY(0);
                else if (Camera.GetOffsetY() > mapHeight - TOP_OF_SCREEN - BOTTOM_OF_SCREEN)
                    Camera.SetOffsetY(mapHeight - TOP_OF_SCREEN - BOTTOM_OF_SCREEN);
                //endregion

                //region Clears bullets on screen (Commented out, don't want to let players do this)
//                    if (handle.input.isKeyPressed(C)) {
//                        objectManager.getGameObjects().removeIf(go -> go.containsKey("Bullet"));
//                    }
                //endregion

                //region Calculate the rotation
                Vector2 player_client_center =
                        new Vector2(player_client.getPosX() - Camera.GetOffsetX() + player_client.getWidth() / 2,
                                player_client.getPosY() - Camera.GetOffsetY() + player_client.getHeight() / 2);
                Vector2 direction =
                        new Vector2(Input.mousePosition.getX(), Input.mousePosition.getY())
                                .subtract(player_client_center);

                Affine rotate = new Affine();
                short deg = (short) Math.toDegrees(Math.atan2(direction.getY(), direction.getX()));
                rotate.appendRotation(deg, player_client_center.getX(), player_client_center.getY());
                player_client.setRotation(deg);
                player_client.setAffine(rotate);
                //endregion

                //region Checks if the player is holding the ball
                if (ball.getBounds().intersects(player_client.circle.getBoundsInLocal()) && !ballShot)
                    player_client.holdsBall = true;

                if (player_client.holdsBall) {
                    double ball_pos_x = Math.cos(Math.atan2(direction.getY(), direction.getX()));
                    double ball_pos_y = Math.sin(Math.atan2(direction.getY(), direction.getX()));
                    Vector2 direction_of_ball = new Vector2(ball_pos_x * 35, ball_pos_y * 35);
                    Vector2 ballPos = new Vector2(player_client.circle.getCenterX()-13,
                            player_client.circle.getCenterY()-10).add(direction_of_ball);
                    ball.setPosition(ballPos);
                    ballShot = false;
                }
                //endregion

                //region Handles player movement
                if (player_client.stand == 0) {
                    if (Input.isKeyReleased(A) && Input.isKeyReleased(D)) {
                        player_client.getVelocity().setX(0);
                    }
                    if (Input.isKeyReleased(W) && Input.isKeyReleased(S)) {
                        player_client.getVelocity().setY(0);
                    }
                    if (Input.isKeyPressed(A)) {
                        player_client.getVelocity().setX(-MOVEMENT_SPEED);
                    }
                    if (Input.isKeyPressed(D)) {
                        player_client.getVelocity().setX(MOVEMENT_SPEED);
                    }
                    if (Input.isKeyPressed(W)) {
                        player_client.getVelocity().setY(-MOVEMENT_SPEED);
                    }
                    if (Input.isKeyPressed(S)) {
                        player_client.getVelocity().setY(MOVEMENT_SPEED);
                    }
                }
                //endregion

                //region Shoots a bullet or the ball
                player_client.setBulletShot(false);
                if (ballShot) ballShot = false;
                if (Input.mouseButtonPressed.get(MouseButton.PRIMARY) && bulletLimiter == 0) {
                    double bullet_pos_x = Math.cos(Math.atan2(direction.getY(), direction.getX()));
                    double bullet_pos_y = Math.sin(Math.atan2(direction.getY(), direction.getX()));
                    Vector2 shotVelocity = new Vector2(bullet_pos_x, bullet_pos_y).multiply(200);

                    if (!player_client.holdsBall) {
                        Vector2 direction_of_gun = new Vector2(bullet_pos_x*32, bullet_pos_y*32);
                        Vector2 bulletPos = new Vector2(player_client.getPosX() + player_client.getHeight() / 2,
                                player_client.getPosY() + player_client.getWidth() / 2).add(direction_of_gun);

                        editObjectManager
                                (0, 0, bulletPos, shotVelocity, player_client.getIpOfClient());
                        player_client.setBulletShot(true);
                    } else {
                        boolean inWall = false;
                        for (Bricks block : MapRender.get_list())
                            if (ball.getBounds().intersects(block.getBounds().getBoundsInLocal()))
                                inWall = true;
                        if (!inWall) {
                            ball.setVelocity(new Vector2(shotVelocity).multiply(1.5));
                            ball.setAcceleration(new Vector2(shotVelocity).divide(100));
                            player_client.holdsBall = false;
                            ballShot = true;
                        }
                    }
                    bulletLimiter = 5;
                } else if (bulletLimiter > 0) bulletLimiter--;
                //endregion

                //region Collision detection
//                Player collision
                for (Bricks block : MapRender.get_list())
                    if (block.getBounds().intersects(player_client.circle.getBoundsInLocal()))
                        player_client.collision(block, elapsedTime);

//                Bullet collision
                editObjectManager(1, 0, null, null, null);

//                Ball collision
                ballWallBounce();
                //endregion

                //region Updates position of all game objects locally (has to go after collision)
                editObjectManager(2, elapsedTime, null, null, null);
                //endregion

                //region Re-renders all game objects
                graphicsContext.clearRect(0, 0, GAME_WINDOW_WIDTH, GAME_WINDOW_HEIGHT);

                MapRender.render(graphicsContext);

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

                //region Sends the client's position and whether they've shot a bullet
                Client.sendPacket(player_client.getPacketToSend(), Client.getUniquePort());
                //endregion

                lastUpdate = currentNanoTime;
            }
        }.start();

        root.requestFocus();
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        System.out.println("Client stopped");
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
        root.getChildren().add(audio.getMenuBar());
        //endregion

        //region Key input listener setup
        root.addEventHandler(KeyEvent.ANY, new KeyboardInput());
        root.addEventHandler(MouseEvent.ANY, new MouseInput());
        scene.setCursor(Cursor.NONE);
        //endregion

        //region Map loading
        try {
            MapRender.loadLevel("res/map.png");
        } catch (Exception e) {
            System.out.println("An Exception occurred in the loading of the map!" + Arrays.toString(e.getStackTrace()));
        }
        //endregion

        //region Gets width and height of the map
        try {
            BufferedImage map = ImageIO.read(new File("res/map.png"));
            mapWidth = map.getWidth() * 40;
            mapHeight = map.getHeight() * 40;
        } catch (IOException e) {
            e.printStackTrace();
        }
        //endregion
    }

    public void initialisePlayers() {
        Client.receivePositions();
        for (String ip : listOfClients.connectedIPs) {
            if (ip.equals(player_client.getIpOfClient())) {
                playerList.put(ip, player_client);
                continue;
            }

            PositionPacket theDoubleValues = listOfClients.posList.get(ip);
            Vector2 pos = new Vector2(theDoubleValues.orgPosX, theDoubleValues.orgPosY);
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

        //region Updates info of other players/bots
        for (String ip : listOfClients.connectedIPs) {
            Player enemy = playerList.get(ip);
            if (ip.equals(player_client.getIpOfClient()) || enemy == null)
                continue;

            PositionPacket theDoubleValues = listOfClients.posList.get(ip);

            //region Enemies' rotation/position/velocity
            Affine rotate = new Affine();
            rotate.appendRotation(theDoubleValues.degrees,
                    theDoubleValues.orgPosX - Camera.GetOffsetX() + player_client.getWidth() / 2,
                    theDoubleValues.orgPosY - Camera.GetOffsetY() + player_client.getHeight() / 2);

            enemy.setAffine(rotate);
            enemy.setVelocity(theDoubleValues.velX, theDoubleValues.velY);
            enemy.setPosition(theDoubleValues.orgPosX, theDoubleValues.orgPosY);
            //endregion

            if (!theDoubleValues.bulletShot)
                continue;

            //region Enemies' bullets
            double degreeRadians = Math.toRadians(theDoubleValues.degrees);
            double bullet_pos_x = Math.cos(degreeRadians);
            double bullet_pos_y = Math.sin(degreeRadians);
            Vector2 direction_of_gun = new Vector2(bullet_pos_x*32, bullet_pos_y*32);

            Vector2 bulletPos = new Vector2(theDoubleValues.orgPosX + player_client.getHeight() / 2,
                    theDoubleValues.orgPosY + player_client.getWidth() / 2).add(direction_of_gun);

            Vector2 bulletVel = new Vector2(bullet_pos_x, bullet_pos_y).multiply(200);

            editObjectManager(0, 0, bulletPos, bulletVel, "enemy");
            //endregion
        }
        //endregion

        //region Updates the ball's position

        //endregion
    }

    private void ballWallBounce() {
        for (Bricks block : MapRender.get_list()) {
            if (!ball.getBounds().intersects(block.getBounds().getBoundsInLocal()) || player_client.holdsBall)
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

    private synchronized void editObjectManager
            (int operation, double elapsedTime, Vector2 bp, Vector2 bv, String shooter) {
        switch (operation) {
            case 0 : { //add bullets
                new Bullet(bp, bv, shooter);
                break;
            } //Add bullets

            case 1 : { //remove bullets if needed
                HashSet<GameObject> crash_bullet_list = new HashSet<>();
                for (GameObject bullet : ObjectManager.getGameObjects()) {
                    if (!bullet.getKeys().contains("Bullet"))
                        continue;

                    for (Bricks block : MapRender.get_list())
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
                    go.update(elapsedTime);
                break;
            } //Update object positions
        }
    }
}