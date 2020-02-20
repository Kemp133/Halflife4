package com.halflife3.Networking.Client;

import com.halflife3.GameUI.AudioForGame;
import com.halflife3.Controller.Input;
import com.halflife3.Controller.KeyHandle;
import com.halflife3.Controller.MouseInput;
import com.halflife3.Controller.ObjectManager;
import com.halflife3.Model.*;
import com.halflife3.Model.Interfaces.IRenderable;
import com.halflife3.Model.Interfaces.IUpdateable;
import com.halflife3.Networking.Packets.PositionPacket;
import com.halflife3.Networking.Server.Server;
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

    //region Variables
    static Input input;
    private static Pane root;
    private static ObjectManager objectManager;
    private static Player player_client; //Can get IP, Position, stateOfAI
    private static HashMap<String, Player> enemyList;

    public boolean running = false;
    private final int FPS = 30;
    private int bulletLimiter = 6;
    private long nSecPerFrame = Math.round(1.0/FPS * 1e9);
    //endregion

    //region
    private Stage window = null;
    private boolean flag = false;
    public ClientGame(){}

    public ClientGame(Stage window){
        this.window = window;
        flag = true;
    }
    //endregion

    public void getStarted() {

        enemyList = new HashMap<>();

        Client clientNetwork = new Client();
        clientNetwork.joinGroup();
        clientNetwork.getHostInfo();
        clientNetwork.start();

        objectManager = new ObjectManager();
        input = new Input();
        root = new Pane();

        Vector2 startPos = clientNetwork.getStartingPosition();
        Vector2 startVel = new Vector2(0, 0);
        player_client = new Player(startPos, startVel, 0, objectManager);
        player_client.setIpOfClient(clientNetwork.getClientAddress().toString());
        player_client.setAI(false);

        Client.receivePositions();
        for (String ip : Client.listOfClients.connectedIPs) {
            if (!ip.equals(player_client.getIpOfClient())) {
                PositionPacket theDoubleValues = Client.listOfClients.posList.get(ip);
                Vector2 pos = new Vector2(theDoubleValues.orgPosX, theDoubleValues.orgPosY);
                Vector2 vel = new Vector2(theDoubleValues.velX, theDoubleValues.velY);
                Player enemy = new Player(pos, vel, theDoubleValues.rotation, objectManager);
                try { enemy.setImage("res/Player_pic.png"); } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                enemy.setIpOfClient(ip);
                enemyList.put(ip, enemy);
            }
        }

        if(flag){
            flag = false;
            try {
                this.start(this.window);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else
            launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("HalfLife 3 : Man in Black");  // change for name
        Canvas canvas = new Canvas(800, 600);
        root.getChildren().add(canvas);
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        player_client.setImage("res/Player_pic.png");
        GraphicsContext graphicsContext = canvas.getGraphicsContext2D();

        //region Background setup
        FileInputStream bgPNG = new FileInputStream("res/background_image.png");
        Image image = new Image(bgPNG, 40, 40, true, true);
        BackgroundImage myBI = new BackgroundImage(image,
                BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT);
        root.setBackground(new Background(myBI));
        //endregion

        //region Add audio into game
        AudioForGame audio = new AudioForGame();
        audio.getMenu().getItems().add(audio.getMute());
        audio.getSlider1().setHideOnClick(false);
        audio.getMenu().getItems().add(audio.getSlider1());
        audio.getMenuBar().getMenus().add(audio.getMenu());
        audio.getBattle_music().setAutoPlay(true);
        audio.getBattle_music().setMute(false);
        audio.getBattle_music().setOnEndOfMedia(() -> {
            audio.getBattle_music().seek(Duration.ZERO);
            audio.getBattle_music().play();
        });
        audio.getMute().setOnAction(actionEvent -> audio.swtichMute());
        audio.getSlider1().setOnAction(actionEvent -> audio.volumeControl(audio.getVolume()));
        root.getChildren().add(audio.getMenuBar());
        //endregion

        //region Key input listener setup
        KeyHandle handle = new KeyHandle();
        root.setOnKeyPressed(handle);
        root.setOnKeyReleased(handle);
        root.addEventHandler(MouseEvent.ANY, new MouseInput(input));
        scene.setCursor(Cursor.NONE);
        //endregion

        //region Initialise cursor
        Crosshair cursor = new Crosshair(input.mousePosition, new Vector2(0, 0), (short)0, objectManager, input);
        //endregion

        //region Map loading
        MapRender map = new MapRender(objectManager);
        map.SetMap("res/map.png");
        map.loadLevel();
        //endregion

        final long[] startNanoTime = {System.nanoTime()};

        //region Updates the position of all enemies
        running = true;
        new Thread(() -> {
            double serverNanoTime = System.nanoTime();
            while (running) {
                if (System.nanoTime() - serverNanoTime > Math.round(1.0/FPS * 1e9)) {
                    Client.receivePositions();

                    HashSet<String> toRemove = getPlayersToDestroy(Server.botNames, Client.listOfClients.connectedIPs);
                    if (toRemove != null) {
                        for (String removeThis : toRemove) {
                            if (enemyList.containsKey(removeThis)) {
                                //Player nPlayer = enemyList.get(removeThis);
                                enemyList.get(removeThis).selfDestroy();
                                enemyList.remove(removeThis);
                                for (String ip : Client.listOfClients.connectedIPs) {
                                    if (!enemyList.containsKey(ip)) {
                                        enemyList.put(ip, player_client);
                                    }
                                }
                            }
                        }
                    }

                    for (String ip : Client.listOfClients.connectedIPs) {
                        if (!ip.equals(player_client.getIpOfClient())) {
                            PositionPacket theDoubleValues = Client.listOfClients.posList.get(ip);
                            Vector2 pos = new Vector2(theDoubleValues.orgPosX, theDoubleValues.orgPosY);
                            Vector2 vel = new Vector2(theDoubleValues.velX, theDoubleValues.velY);

                            enemyList.get(ip).setVelocity(vel);
                            enemyList.get(ip).setPosition(pos);
                        }
                    }

                    serverNanoTime = System.nanoTime();
                }
            }
        }).start();
        //endregion

        AnimationTimer game = new AnimationTimer() {
            private long lastUpdate = 0;
            public void handle(long currentNanoTime) {
                if (currentNanoTime - lastUpdate > nSecPerFrame) {
                    //region Calculate time since last update.
                    double elapsedTime = (currentNanoTime - startNanoTime[0]) / 1000000000.0;
                    startNanoTime[0] = currentNanoTime;
                    //endregion

                    //region Calculate the radius
                    Vector2 player_client_center = new Vector2(player_client.getX() + (player_client.width/4),
                            player_client.getY() + (player_client.height/2));
                    Vector2 direction = new Vector2(input.mousePosition.getX(), input.mousePosition.getY())
                            .subtract(player_client_center);
                    Affine rotate = new Affine();


                    double bullet_pos_x = Math.cos(Math.atan2(direction.getY(),direction.getX()))*32;
                    double bullet_pos_y = Math.sin(Math.atan2(direction.getY(),direction.getX()))*32;
                    Vector2 direction_of_gun = new Vector2(bullet_pos_x, bullet_pos_y);
                    //endregion

                    //region Handles player movement
                    if (handle.input.isKeyReleased(A) && handle.input.isKeyReleased(D))
                        player_client.getVelocity().setX(0);
                    if (handle.input.isKeyReleased(W) && handle.input.isKeyReleased(S))
                        player_client.getVelocity().setY(0);

                    if (handle.input.isKeyPressed(A))
                        player_client.getVelocity().setX(-100);
                    if (handle.input.isKeyPressed(D))
                        player_client.getVelocity().setX(100);
                    if (handle.input.isKeyPressed(W))
                        player_client.getVelocity().setY(-100);
                    if (handle.input.isKeyPressed(S))
                        player_client.getVelocity().setY(100);
                    //endregion

                    //region Clears bullets on screen
                    if (handle.input.isKeyPressed(C)) {
                        objectManager.getGameObjects().removeIf(go -> go.containsKey("Bullet"));
                    }
                    //endregion

                    //region Create a new bullet
                    if (input.mouseButtonPressed.get(MouseButton.PRIMARY) && bulletLimiter == 0) {

                        Vector2 bulletPos =new Vector2(player_client.getX() + player_client.width/2,player_client.getY()+player_client.height/2).add(direction_of_gun);
                        //Vector2 bulletPos = player_client_center.add(new Vector2(20,20));
                        Vector2 bulletVel = new Vector2(input.mousePosition.getX(), input.mousePosition.getY())
                                                .subtract(bulletPos).normalise().multiply(200);

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
                    HashSet<GameObject> crash_bullet_list = new HashSet<>();
                    boolean player_hit_block = false;
                    for (Bricks block : MapRender.get_list()) {
                        if (block.GetBounds().intersects(player_client.rectangle.getBoundsInLocal())) {
                            player_hit_block = true;
                        }
                    }
                    boolean Bullet_hit_wall = false;
                    boolean Bullet_hit_player = false;
                    for(GameObject go:objectManager.getGameObjects()){
                        if (go.getKeys().contains("Bullet")){
                            for(Bricks block : MapRender.get_list()){
                                if(go.GetBounds().intersects(block.GetBounds().getBoundsInLocal())){
                                    Bullet_hit_wall = true;
                                    crash_bullet_list.add(go);
                                }
                            }
                            if(go.GetBounds().intersects(player_client.rectangle.getBoundsInLocal())){
                                Bullet_hit_player = true;
                                crash_bullet_list.add(go);
                            }
                        }
                    }
                    for(GameObject bullet:crash_bullet_list){
                        bullet.remove();
                    }
                    //endregion

                    //region If a collision happens - moves the player back
                    player_client.collision(player_hit_block, elapsedTime);
                    //endregion

                    //region Clears screen
                    graphicsContext.clearRect(0, 0, 800, 600);
                    //endregion

                    //double degree_of_gun = Math.toDegrees(Math.atan2(direction.getY(),direction.getX())) + Math.toDegrees(Math.atan2(1,3));
                    //Vector2 direction_of_gun = (Math.cos(degree_of_gun)*9.5, Math.sin(degree_of_gun));

                    //region Rotation of the player
                    rotate.appendRotation(Math.toDegrees(Math.atan2(direction.getY(), direction.getX())), player_client_center.getX(), player_client_center.getY());
                    player_client.setRotate(rotate);
                    //endregion

                    //region Renders all game objects
                    for (IRenderable go : objectManager.getGameObjects()) {
                        go.render(graphicsContext);
                    }
                    //endregion

                    //TODO: Send the client's bullets' positions & velocities to the server
                    //region Sends the client's position now
                    Client.sendPacket(player_client.getPacketToSend(), Client.getUniquePort());
                    //endregion

                    lastUpdate = currentNanoTime;
                }
            }
        };
        game.start();

        root.requestFocus();
        primaryStage.show();

    }

    @Override
    public void stop() throws Exception {
        super.stop();
        Client.close();
        running = false;
    }

    public HashSet<String> getPlayersToDestroy(String[] botNames, HashSet<String> IPs) {
        HashSet<String> playersToRemove = new HashSet<>();

        for (String bot : botNames)
            if (!IPs.contains(bot))
                playersToRemove.add(bot);

        return playersToRemove;
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
