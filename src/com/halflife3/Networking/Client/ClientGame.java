package com.halflife3.Networking.Client;

import com.halflife3.Controller.Input;
import com.halflife3.Controller.KeyHandle;
import com.halflife3.Controller.MouseInput;
import com.halflife3.Controller.ObjectManager;
import com.halflife3.GameUI.AudioForGame;
import com.halflife3.Model.*;
import com.halflife3.Model.Interfaces.IRenderable;
import com.halflife3.Model.Interfaces.IUpdateable;
import com.halflife3.Networking.Packets.PositionPacket;
import com.halflife3.View.MapRender;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.transform.Affine;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static javafx.scene.input.KeyCode.*;

public class ClientGame extends Application {

    private final int FPS = 30;
    private final int INC_PACKETS_PER_SECOND = 60;
    private final int OUT_PACKETS_PER_SECOND = 60;

    //region Other variables
    static Input input;
    private static Pane root;
    private static KeyHandle handle;
    private static Crosshair cursor;
    private static ObjectManager objectManager;
    private static Player player_client; //Can get IP, Position, stateOfAI
    private static HashMap<String, Player> playerList;
    private Stage window = null;
    private boolean flag = false;
    public boolean running = false;
    private int bulletLimiter = 6;
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
        objectManager = new ObjectManager();
        input = new Input();
        root = new Pane();
        //endregion

        //region Initialise this player
        Vector2 startPos = clientNetwork.getStartingPosition();
        Vector2 startVel = new Vector2(0, 0);
        player_client = new Player(startPos, startVel, 0, objectManager);
        player_client.setIpOfClient(clientNetwork.getClientAddress().toString());
        player_client.setAI(false);
        try { player_client.setImage("res/Player_pic.png"); } catch (FileNotFoundException e) {
            System.out.println("Could not find file in path: 'res/Player_pic.png'");
        }
        try { player_client.setImage_w("res/Player_walking.png"); } catch (FileNotFoundException e) {
            System.out.println("Could not find file in path: 'res/Player_walking.png'");
        }
        //endregion

        //region Wait until Server acknowledges Player connection
        do {
            Client.receivePositions();
        } while (!Client.listOfClients.connectedIPs.contains(player_client.getIpOfClient()));
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
//                    double x = System.nanoTime();
                    updateEnemies();
//                    System.out.println("Time took to update enemy positions: " + (System.nanoTime() - x));
                    serverNanoTime = System.nanoTime();
                }
            }
        }).start();
        //endregion

        new AnimationTimer() {
            private long lastUpdate = 0;
            private int packetSendCounter = OUT_PACKETS_PER_SECOND;
            double startNanoTime = System.nanoTime();
            Vector2 camera_offset = new Vector2();

            public void handle(long currentNanoTime) {
                if (currentNanoTime - lastUpdate > Math.round(1.0/FPS * 1e9)) {
                    //region Calculate time since last update.
                    double elapsedTime = (currentNanoTime - startNanoTime) / 1e9;
                    startNanoTime = currentNanoTime;
                    //endregion

                    //region Camera offset
                    camera_offset.setX(player_client.getPosition().getX() - 9*40);
                    camera_offset.setY(player_client.getPosition().getY() - 7*40);
                    if(camera_offset.getX()<0)
                        camera_offset.setX(0);
                    if(camera_offset.getX() > 25*40) //map width subtract half of window width
                        camera_offset.setX(25*40);
                    if(camera_offset.getY() < 0)
                        camera_offset.setY(0);
                    if(camera_offset.getY() > 30*40) //map height subtract half of window height
                        camera_offset.setY(30*40);
                    //endregion

                    //region Handles player movement
                    if (handle.input.isKeyReleased(A) && handle.input.isKeyReleased(D)) {
                        player_client.getVelocity().setX(0);
                        player_client.setIs_moving(false);
                    }
                    if (handle.input.isKeyReleased(W) && handle.input.isKeyReleased(S)){
                        player_client.getVelocity().setY(0);
                        player_client.setIs_moving(false);
                    }
                    if (handle.input.isKeyPressed(A)) {
                        player_client.getVelocity().setX(-100);
                        player_client.setIs_moving(true);
                    }
                    if (handle.input.isKeyPressed(D)) {
                        player_client.getVelocity().setX(100);
                        player_client.setIs_moving(true);
                    }
                    if (handle.input.isKeyPressed(W)) {
                        player_client.getVelocity().setY(-100);
                        player_client.setIs_moving(true);
                    }
                    if (handle.input.isKeyPressed(S)) {
                        player_client.getVelocity().setY(100);
                        player_client.setIs_moving(true);
                    }
                    //endregion

                    //region Clears bullets on screen
                    if (handle.input.isKeyPressed(C)) {
                        objectManager.getGameObjects().removeIf(go -> go.containsKey("Bullet"));
                    }
                    //endregion

                    //region Calculate the rotation
                    Vector2 player_client_center = new Vector2(player_client.getX()-camera_offset.getX() + 18,
                                                            player_client.getY()-camera_offset.getY() + 18);
                    Vector2 direction = new Vector2(input.mousePosition.getX(), input.mousePosition.getY())
                                            .subtract(player_client_center);
//                    Player rotation
                    Affine rotate = new Affine();
                    rotate.appendRotation(Math.toDegrees(Math.atan2(direction.getY(), direction.getX())), player_client_center.getX(), player_client_center.getY());
                    player_client.setRotate(rotate);

                    double bullet_pos_x = Math.cos(Math.atan2(direction.getY(),direction.getX()))*32;
                    double bullet_pos_y = Math.sin(Math.atan2(direction.getY(),direction.getX()))*32;
                    Vector2 direction_of_gun = new Vector2(bullet_pos_x, bullet_pos_y);
                    //endregion

                    //region Create a new bullet
                    if (input.mouseButtonPressed.get(MouseButton.PRIMARY) && bulletLimiter == 0) {
                        Vector2 bulletPos =new Vector2(player_client.getX() + player_client.width/2,player_client.getY()+player_client.height/2).add(direction_of_gun);
                        Vector2 bulletVel = new Vector2(cursor.getX()+camera_offset.getX(), cursor.getY()+camera_offset.getY())
                                                .subtract(player_client.getPosition()).normalise().multiply(200);

                        new Bullet(bulletPos, bulletVel, (short)0, objectManager);
                        bulletLimiter = 6;
                    } else if (bulletLimiter > 0) bulletLimiter--;
                    //endregion

                    //region Updates position of all game objects locally
                    for(IUpdateable go : objectManager.getGameObjects()) {
                        go.update(elapsedTime);
                    }

                    //endregion

                    //region Collision detection
                    boolean player_hit_block = false;
                    for (Bricks block : MapRender.get_list()) {
                        if (block.GetBounds().intersects(player_client.circle.getBoundsInLocal())) {
                            player_hit_block = true;
                        }
                    }
//                    If a collision happens - moves the player back
                    player_client.collision(player_hit_block, elapsedTime);

//                    HashSet<GameObject> crash_bullet_list = new HashSet<>();
//                    boolean Bullet_hit_wall = false;
//                    boolean Bullet_hit_player = false;
//                    for(GameObject go:objectManager.getGameObjects()){
//                        if (go.getKeys().contains("Bullet")){
//                            for(Bricks block : MapRender.get_list()){
//                                if(go.GetBounds().intersects(block.GetBounds().getBoundsInLocal())){
//                                    Bullet_hit_wall = true;
//                                    crash_bullet_list.add(go);
//                                }
//                            }
//                            if(go.GetBounds().intersects(player_client.circle.getBoundsInLocal())){
//                                Bullet_hit_player = true;
//                                crash_bullet_list.add(go);
//                            }
//                        }
//                    }
//                    for(GameObject bullet:crash_bullet_list){
//                        bullet.remove();
//                    }
                    //endregion

                    //region Sends the client's position
                    //TODO: Send the client's bullets' velocities to the server
                    if (packetSendCounter == 0) {
                        Client.sendPacket(player_client.getPacketToSend(), Client.getUniquePort());
                        packetSendCounter = OUT_PACKETS_PER_SECOND;
                    } else if (packetSendCounter > 0) packetSendCounter--;
                    //endregion

                    //region Re-renders all game objects
                    graphicsContext.clearRect(0, 0, 800, 600);
                    for (IRenderable go : objectManager.getGameObjects()) {
                        go.render(graphicsContext,camera_offset);
                    }
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
        Client.disconnect();
        running = false;
        super.stop();
    }

    private void gameInit(Scene scene) {
        //region Background setup
        FileInputStream bgPNG = null;
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
        handle = new KeyHandle();
        root.setOnKeyPressed(handle);
        root.setOnKeyReleased(handle);
        root.addEventHandler(MouseEvent.ANY, new MouseInput(input));
        scene.setCursor(Cursor.NONE);
        //endregion

        //region Initialise cursor
        cursor = new Crosshair(input.mousePosition, new Vector2(0, 0), (short)0, objectManager, input);
        //endregion

        //region Map loading
        MapRender map = new MapRender(objectManager);
        try {
            map.SetMap("res/soccer.png");
            map.loadLevel();
        } catch (FileNotFoundException e) {
            System.out.println("Could not find file in path 'res/map.png'");
        }
        //endregion
    }

    public void initialisePlayers() {
        Client.receivePositions();
        for (String ip : Client.listOfClients.connectedIPs) {
            if (ip.equals(player_client.getIpOfClient())) {
                playerList.put(ip, player_client);
                continue;
            }

            PositionPacket theDoubleValues = Client.listOfClients.posList.get(ip);
            Vector2 pos = new Vector2(theDoubleValues.orgPosX, theDoubleValues.orgPosY);
            Vector2 vel = new Vector2(theDoubleValues.velX, theDoubleValues.velY);
            Player enemy = new Player(pos, vel, theDoubleValues.rotation, objectManager);
            try { enemy.setImage("res/Player_pic.png"); } catch (FileNotFoundException e) {
                System.out.println("Could not find file in path: 'res/Player_pic.png'");
            }
            enemy.setIpOfClient(ip);
            playerList.put(ip, enemy);
        }
    }

    public void updateEnemies() {
        ArrayList<String> playerKeys = new ArrayList<>();

        Client.receivePositions();

        for (HashMap.Entry<String, Player> player : playerList.entrySet())
            playerKeys.add(player.getKey());

        //region Replaces Bots <-> Players
        for (String player : playerKeys) {
//            If bot name/player IP is stored locally - continue
            if (Client.listOfClients.connectedIPs.contains(player))
                continue;

//            If server list has been updated - reset the odd player's position and velocity
            playerList.get(player).resetPosition();
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

        for (String ip : Client.listOfClients.connectedIPs) {
            if (ip.equals(player_client.getIpOfClient()))
                continue;

            PositionPacket theDoubleValues = Client.listOfClients.posList.get(ip);
            Vector2 vel = new Vector2(theDoubleValues.velX, theDoubleValues.velY);
            Vector2 pos = new Vector2(theDoubleValues.orgPosX, theDoubleValues.orgPosY);
            if (playerList.get(ip) != null) {
                playerList.get(ip).setVelocity(vel);
                playerList.get(ip).setPosition(pos);
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
