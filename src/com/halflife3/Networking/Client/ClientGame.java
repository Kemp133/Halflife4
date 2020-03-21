package com.halflife3.Networking.Client;

import com.halflife3.Controller.*;
import com.halflife3.GameUI.AudioForGame;
import com.halflife3.Model.*;
import com.halflife3.Model.Interfaces.*;
import com.halflife3.Networking.Packets.PositionPacket;
import com.halflife3.View.Camera;
import com.halflife3.View.MapRender;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.canvas.*;
import javafx.scene.image.Image;
import javafx.scene.input.*;
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
import java.util.*;

import static com.halflife3.Networking.Client.Client.listOfClients;
import static javafx.scene.input.KeyCode.*;

public class ClientGame extends Application {

    private final int FPS = 30;
    private final int INC_PACKETS_PER_SECOND = 30;
    private final int GAME_WINDOW_HEIGHT = 600;
    private final int GAME_WINDOW_WIDTH = 800;
    private final int MOVEMENT_SPEED = 100;

    //region Other variables
    static Input input;
    private static Pane root;
    private static Player player_client;
    private static HashMap<String, Player> playerList;
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
        //endregion

        gameInit(scene);

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

        new AnimationTimer() {
            private long lastUpdate = 0;
            double startNanoTime = System.nanoTime();

            public void handle(long currentNanoTime) {
                if (currentNanoTime - lastUpdate > Math.round(1.0/FPS * 1e9)) {
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

                    //region Handles player movement
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

                    //region Collision detection
//                    Player collision
                    for (Bricks block : MapRender.get_list())
                        if (block.getBounds().intersects(player_client.circle.getBoundsInLocal()))
                            player_client.collision(block, elapsedTime);

//                    Bullet collision
                    editObjectManager(1, 0, null, null);
                    //endregion

                    //region Updates position of all game objects locally (has to go after collision)
                    editObjectManager(2, elapsedTime, null, null);
                    //endregion

                    //region Clears bullets on screen (Commented out, don't want to let players do this)
//                    if (handle.input.isKeyPressed(C)) {
//                        objectManager.getGameObjects().removeIf(go -> go.containsKey("Bullet"));
//                    }
                    //endregion

                    //region Create a new bullet
                    player_client.setBulletShot(false);
                    if (Input.mouseButtonPressed.get(MouseButton.PRIMARY) && bulletLimiter == 0) {
                        double bullet_pos_x = Math.cos(Math.atan2(direction.getY(), direction.getX()));
                        double bullet_pos_y = Math.sin(Math.atan2(direction.getY(), direction.getX()));
                        Vector2 direction_of_gun = new Vector2(bullet_pos_x*32, bullet_pos_y*32);

                        Vector2 bulletPos = new Vector2(player_client.getPosX() + player_client.getHeight() / 2,
                                player_client.getPosY() + player_client.getWidth() / 2).add(direction_of_gun);

                        Vector2 bulletVel = new Vector2(bullet_pos_x, bullet_pos_y).multiply(200);

                        new Bullet(bulletPos, bulletVel);
                        player_client.setBulletShot(true);
                        bulletLimiter = 5;
                    } else if (bulletLimiter > 0) bulletLimiter--;
                    //endregion

                    //region Sends the client's position and whether they've shot a bullet
                    Client.sendPacket(player_client.getPacketToSend(), Client.getUniquePort());
                    //endregion

                    //region Re-renders all game objects
                    graphicsContext.clearRect(0, 0, GAME_WINDOW_WIDTH, GAME_WINDOW_HEIGHT);

                    MapRender.render(graphicsContext);

                    for (IRenderable go : ObjectManager.getGameObjects())
                        go.render(graphicsContext);
                    //endregion

                    lastUpdate = currentNanoTime;
                }
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
            enemy.setSprite("res/Player_pic.png");
            enemy.setIpOfClient(ip);
            playerList.put(ip, enemy);
        }
    }

    public void updateEnemies() {
        Client.receivePositions();

        //region Replaces Bots <-> Players
        ArrayList<String> playerKeys = new ArrayList<>();

        for (HashMap.Entry<String, Player> player : playerList.entrySet())
            playerKeys.add(player.getKey());

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
            if (ip.equals(player_client.getIpOfClient()) || playerList.get(ip) == null)
                continue;

            PositionPacket theDoubleValues = listOfClients.posList.get(ip);

            //region Camera Offset
            double cameraX = player_client.getPosX() - LEFT_END_OF_SCREEN;
            double cameraY = player_client.getPosY() - TOP_OF_SCREEN;

            if (cameraX < 0)
                cameraX = 0;
            else if (cameraX > mapWidth - LEFT_END_OF_SCREEN - RIGHT_END_OF_SCREEN)
                cameraX = mapWidth - LEFT_END_OF_SCREEN - RIGHT_END_OF_SCREEN;

            if (cameraY < 0)
                cameraY = 0;
            else if (cameraY > mapHeight - TOP_OF_SCREEN - BOTTOM_OF_SCREEN)
                cameraY = mapHeight - TOP_OF_SCREEN - BOTTOM_OF_SCREEN;
            //endregion

            //region Enemies' rotation/position/velocity
            Affine rotate = new Affine();
            rotate.appendRotation(theDoubleValues.degrees,
                    theDoubleValues.orgPosX - cameraX + 18,
                    theDoubleValues.orgPosY - cameraY + 18);

            playerList.get(ip).setAffine(rotate);
            playerList.get(ip).setVelocity(theDoubleValues.velX, theDoubleValues.velY);
            playerList.get(ip).setPosition(theDoubleValues.orgPosX, theDoubleValues.orgPosY);
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

            editObjectManager(0, 0, bulletPos, bulletVel);
            //endregion
        }
        //endregion
    }

    private synchronized void editObjectManager(int operation, double elapsedTime, Vector2 bp, Vector2 bv) {
        switch (operation) {
            case 0 : { //add bullets
                new Bullet(bp, bv);
                break;
            }

            case 1 : { //remove bullets if needed
                HashSet<GameObject> crash_bullet_list = new HashSet<>();
                for (GameObject go: ObjectManager.getGameObjects()) {
                    if (!go.getKeys().contains("Bullet"))
                        continue;

                    for (Bricks block : MapRender.get_list())
                        if (go.getBounds().intersects(block.getBounds().getBoundsInLocal()))
                            crash_bullet_list.add(go);

                    for (String ip : listOfClients.connectedIPs)
                        if (go.getBounds().intersects(playerList.get(ip).circle.getBoundsInLocal()))
                            crash_bullet_list.add(go);
                }

                for (GameObject bullet : crash_bullet_list)
                    bullet.destroy();

                break;
            }

            case 2 : {//update object positions
                for (IUpdateable go : ObjectManager.getGameObjects())
                    go.update(elapsedTime);

                break;
            }
        }
    }
}