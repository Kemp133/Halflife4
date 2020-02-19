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

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashSet;

import static javafx.scene.input.KeyCode.*;

public class ClientGame extends Application {

    //region Variables
    static Input input;
    private static Pane root;
    private static ObjectManager objectManager;
    private static Player player_client; //Can get IP, Position, stateOfAI
    private static Player enemy1;
    private static Player enemy2;
    private static Player enemy3;

    private final int FPS = 30;
    private int bulletLimiter = 6;
    private long nSecPerFrame = Math.round(1.0/FPS * 1e9);
    //endregion

    //region for menu --lin
    private Stage window;
    public ClientGame(Stage Window){
        window = Window;
    }
    //end of region

    public void getStarted() throws Exception {
        Client clientNetwork = new Client();
        clientNetwork.joinGroup();
        clientNetwork.getHostInfo();
        clientNetwork.start();

        objectManager = new ObjectManager();
        input = new Input();
        root = new Pane();
        Vector2 startPos = clientNetwork.getStartingPosition();
        Vector2 startVel = new Vector2(0, 0);
        player_client = new Player(startPos, startVel, (short) 0, objectManager);
        player_client.setIpOfClient(clientNetwork.getClientAddress().toString());
        player_client.setAI(false);

        Client.receivePositions();
        for (String ip : Client.listOfClients.connectedIPs) {
            if (!ip.equals(player_client.getIpOfClient())) {
                PositionPacket theDoubleValues = Client.listOfClients.posList.get(ip);

            }

        }
        //TODO: Create new Players with the received positions
        if(window != null)
            this.start(window);
        else
            launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("HalfLife 3");
        Canvas canvas = new Canvas(800, 600);
        root.getChildren().add(canvas);
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        player_client.setImage("res/Player_pic.png");
        //TODO: Set differently hued images to all the players
        GraphicsContext graphicsContext = canvas.getGraphicsContext2D();

        //region Background setup
        FileInputStream bgPNG = new FileInputStream("res/background_image.png");
        Image image = new Image(bgPNG, 40, 40, true, true);
        BackgroundImage myBI = new BackgroundImage(image,
                BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT);
        root.setBackground(new Background(myBI));
        //endregion

        //region to add audio into game
        AudioForGame audio = new AudioForGame();
        audio.getMenu().getItems().add(audio.getMute());
        audio.getSlider1().setHideOnClick(false);
        audio.getMenu().getItems().add(audio.getSlider1());
        audio.getMenuBar().getMenus().add(audio.getMenu());
        audio.getBattle_music().setAutoPlay(true);
        audio.getBattle_music().setMute(false);
        audio.getMenu().setOnAction(actionEvent -> audio.getBattle_music().play());
        audio.getMute().setOnAction(actionEvent -> audio.swtichMute());
        audio.getSlider1().setOnAction(actionEvent -> audio.volumeControl(audio.getVolume()));
        root.getChildren().add(audio.getMenuBar());
        //end region


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

        AnimationTimer game = new AnimationTimer() {
            private long lastUpdate = 0;
            public void handle(long currentNanoTime) {
                if (currentNanoTime - lastUpdate > nSecPerFrame) {
                    //region Calculate time since last update.
                    double elapsedTime = (currentNanoTime - startNanoTime[0]) / 1000000000.0;
                    startNanoTime[0] = currentNanoTime;
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
                        Vector2 bulletPos = new Vector2(player_client.getX() + player_client.width - 7,
                                                        player_client.getY() + player_client.height - 12);
                        Vector2 bulletVel = new Vector2(input.mousePosition.getX(), input.mousePosition.getY())
                                                .subtract(bulletPos).normalise().multiply(200);

                        new Bullet(bulletPos, bulletVel, (short)0, objectManager);
                        bulletLimiter = 6;

                    } else if (bulletLimiter > 0) bulletLimiter--;
                    //endregion

                    Client.receivePositions();
                    //TODO: Update all players' positions

                    //region Updates position of all game objects locally
                    for(IUpdateable go : objectManager.getGameObjects()) {
                        go.update(elapsedTime);
                    }
                    //endregion

                    //region Collision detection
                    HashSet<GameObject> crash_bullet_list = new HashSet<>();
                    boolean player_hit_block = false;
                    for (Bricks block : map.get_list()) {
                        if (block.GetBounds().intersects(player_client.rectangle.getBoundsInLocal())) {
                            player_hit_block = true;
                        }
                    }
                    boolean Bullet_hit_wall = false;
                    boolean Bullet_hit_player = false;
                    for(GameObject go:objectManager.getGameObjects()){
                        if (go.getKeys().contains("Bullet")){
                            for(Bricks block : map.get_list()){
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

                    //region Renders all game objects
                    Vector2 player_client_center = new Vector2(player_client.getX() + (player_client.width/4),
                            player_client.getY() + (player_client.height/2));
                    Vector2 direction = new Vector2(input.mousePosition.getX(), input.mousePosition.getY())
                            .subtract(player_client_center);
                    Affine rotate = new Affine();

                    //double degree_of_gun = Math.toDegrees(Math.atan2(direction.getY(),direction.getX())) + Math.toDegrees(Math.atan2(1,3));
                    //Vector2 direction_of_gun = (Math.cos(degree_of_gun)*9.5, Math.sin(degree_of_gun));

                    rotate.appendRotation(Math.toDegrees(Math.atan2(direction.getY(),direction.getX())), player_client_center.getX(), player_client_center.getY());
                    player_client.setRotate(rotate);
                    //System.out.println(Math.atan2(direction.getY(),direction.getX()));

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
