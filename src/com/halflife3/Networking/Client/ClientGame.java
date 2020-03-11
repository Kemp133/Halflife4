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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

import static com.halflife3.Networking.Client.Client.listOfClients;
import static javafx.scene.input.KeyCode.*;

public class ClientGame extends Application {

    private final int FPS = 30;
    private final int INC_PACKETS_PER_SECOND = 30;

    //region Other variables
    static Input input;
    private static Pane root;
    private static Player player_client;
    private static HashMap<String, Player> playerList;
    private Stage window = null;
    private boolean flag = false;
    public boolean running = false;
    private int bulletLimiter = 5;
    private double bulletMillis;
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
        player_client.setSprite("res/Player_pic.png");
        player_client.setSprite2("res/Player_walking.png");
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
        Canvas canvas = new Canvas(800, 600);
        root.getChildren().add(canvas);
        Scene scene = new Scene(root, 800, 600);
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
                bulletMillis = System.currentTimeMillis();
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
                    Camera.SetOffsetX(player_client.getPosX() - 9*40);
                    Camera.SetOffsetY(player_client.getPosY() - 7*40);
                    if (Camera.GetOffsetX() < 0)
                        Camera.SetOffsetX(0);
                    else if (Camera.GetOffsetX() > 25*40) //map width subtract half of window width
                        Camera.SetOffsetX(25*40);
                    if (Camera.GetOffsetY() < 0)
                        Camera.SetOffsetY(0);
                    else if (Camera.GetOffsetY() > 30*40) //map height subtract half of window height
                        Camera.SetOffsetY(30*40);
                    //endregion

                    //region Handles player movement
                    if (Input.isKeyReleased(A) && Input.isKeyReleased(D)) {
                        player_client.getVelocity().setX(0);
                        player_client.setMoving(false);
                    }
                    if (Input.isKeyReleased(W) && Input.isKeyReleased(S)){
                        player_client.getVelocity().setY(0);
                        player_client.setMoving(false);
                    }
                    if (Input.isKeyPressed(A)) {
                        player_client.getVelocity().setX(-100);
                        player_client.setMoving(true);
                    }
                    if (Input.isKeyPressed(D)) {
                        player_client.getVelocity().setX(100);
                        player_client.setMoving(true);
                    }
                    if (Input.isKeyPressed(W)) {
                        player_client.getVelocity().setY(-100);
                        player_client.setMoving(true);
                    }
                    if (Input.isKeyPressed(S)) {
                        player_client.getVelocity().setY(100);
                        player_client.setMoving(true);
                    }
                    //endregion

                    //region Clears bullets on screen (Commented out, don't want to let players do this)
//                    if (handle.input.isKeyPressed(C)) {
//                        objectManager.getGameObjects().removeIf(go -> go.containsKey("Bullet"));
//                    }
                    //endregion

                    //region Calculate the rotation
                    Vector2 player_client_center =
                            new Vector2(player_client.getPosX() - Camera.GetOffsetX() + 20,
                                    player_client.getPosY() - Camera.GetOffsetY() + 18);
                    Vector2 direction =
                            new Vector2(Input.mousePosition.getX(), Input.mousePosition.getY())
                                    .subtract(player_client_center);

//                    Player rotation
                    Affine rotate = new Affine();
                    short deg = (short) Math.toDegrees(Math.atan2(direction.getY(), direction.getX()));
                    rotate.appendRotation(deg, player_client_center.getX(), player_client_center.getY());
                    player_client.setRotation(deg);
                    player_client.setAffine(rotate);
                    //endregion

                    //region Create a new bullet

//                    player_client.setBulletShot(false); TODO: Why this no work???
                    if (Input.mouseButtonPressed.get(MouseButton.PRIMARY) && bulletLimiter == 0) {
                        double bullet_pos_x = Math.cos(Math.atan2(direction.getY(), direction.getX()));
                        double bullet_pos_y = Math.sin(Math.atan2(direction.getY(), direction.getX()));
                        Vector2 direction_of_gun = new Vector2(bullet_pos_x*32, bullet_pos_y*32);

                        Vector2 bulletPos = new Vector2(player_client.getPosX() + player_client.getHeight() / 2,
                                player_client.getPosY() + player_client.getWidth() / 2).add(direction_of_gun);

                        Vector2 bulletVel = new Vector2(bullet_pos_x, bullet_pos_y).multiply(200);

                        new Bullet(bulletPos, bulletVel);
//                        player_client.setBulletShot(true); TODO: Why this no work???
                        bulletLimiter = 5;
                    } else if (bulletLimiter > 0) bulletLimiter--;
                    //endregion

                    //region Updates position of all game objects locally
                    editObjectManager(2, elapsedTime, null, null);
                    //endregion

                    //region Collision detection
                    boolean player_hit_block = false;
                    for (Bricks block : MapRender.get_list())
                        if (block.getBounds().intersects(player_client.circle.getBoundsInLocal()))
                            player_hit_block = true;

                    player_client.collision(player_hit_block, elapsedTime);

                    editObjectManager(1, 0, null, null);
                    //endregion

                    //region Sends the client's position and whether they've shot a bullet
                    Client.sendPacket(player_client.getPacketToSend(), Client.getUniquePort());
                    //endregion

                    //region Re-renders all game objects
                    graphicsContext.clearRect(0, 0, 800, 600);

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
            double cameraX = player_client.getPosX() - 9*40;
            double cameraY = player_client.getPosY() - 7*40;

            if (cameraX < 0) cameraX = 0;
            else if (cameraX > 25*40) cameraX = 25*40;

            if (cameraY < 0) cameraY = 0;
            else if (cameraY > 30*40) cameraY = 30*40;
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

//            if (!theDoubleValues.bulletShot) TODO: Why is this not working
//                continue;

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
            }

            case 1 : { //remove bullets
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
            }

            case 2 : {//update object positions
                for(IUpdateable go : ObjectManager.getGameObjects()) {
                    go.update(elapsedTime);
                }
            }
        }
    }

    //region Framework to get only some specific game objects
    private ArrayList<GameObject> getPlayersAndBullets(HashSet<GameObject> allObjects) {
        ArrayList<GameObject> playersAndBullets = new ArrayList<>();

        while (allObjects.iterator().hasNext()) {
            GameObject object = allObjects.iterator().next();
            if (object.getKeys().contains("player")) {
                playersAndBullets.add(object);
            }
        }

        return playersAndBullets;
    }
    //endregion
}