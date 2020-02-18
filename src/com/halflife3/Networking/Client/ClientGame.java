package com.halflife3.Networking.Client;

import com.halflife3.Controller.Input;
import com.halflife3.Controller.KeyHandle;
import com.halflife3.Controller.MouseInput;
import com.halflife3.Controller.ObjectManager;
import com.halflife3.Model.*;
import com.halflife3.Model.Interfaces.IRenderable;
import com.halflife3.Model.Interfaces.IUpdateable;
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
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;

import static javafx.scene.input.KeyCode.*;
import static javafx.scene.input.KeyCode.SPACE;

public class ClientGame extends Application {

    //region Variables
    private Client clientNetwork;
    private int uPort;

    static Input input;
    private static Pane root;
    private static ObjectManager objectManager;
    private static Player player_client; //Can get IP, Position, stateOfAI

    private final int FPS = 60;
    private long nSecPerFrame = Math.round(1.0/FPS * 1e9);
    //endregion

    public void getStarted() {
        clientNetwork = new Client();
        clientNetwork.joinGroup();
        clientNetwork.getHostInfo();
        clientNetwork.start();

        objectManager = new ObjectManager();
        input = new Input();
        root = new Pane();
        Vector2 startPos = clientNetwork.getStartingPosition();
        Vector2 startVel = new Vector2(0, 0);
        player_client = new Player(startPos, startVel, (short) 0, objectManager);
        player_client.setIpOfClient(clientNetwork.getClientAddress());
        player_client.setAI(false);

        //TODO: Get the list of connected players and setup their Player GameObjects

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
            private int bulletLimiter = 0;
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

                    //TODO: Limit number of bullets player can shoot
                    //region Create a new bullet
                    if(input.mouseButtonPressed.get(MouseButton.PRIMARY) && bulletLimiter % 19 == 0) {
                        Vector2 bulletPos = new Vector2(player_client.getX() + player_client.width,
                                                        player_client.getY() + player_client.height);
                        Vector2 bulletVel = new Vector2(input.mousePosition.getX(), input.mousePosition.getY())
                                                .subtract(player_client.getPosition()).normalise().multiply(200);

                        new Bullet(bulletPos, bulletVel, (short)0, objectManager);
                    }
                    bulletLimiter++;
                    if (bulletLimiter >= 60) bulletLimiter = 1;
                    //endregion

                    //TODO: Get positions of all clients from the Server
                    //region Updates position of all game objects locally
                    for(IUpdateable go : objectManager.getGameObjects()) {
                        go.update(elapsedTime);
                    }
                    //endregion

                    //region Collision detection
                    boolean player_hit_block = false;
                    for (Bricks block : map.get_list()) {
                        if (block.GetBounds().intersects(player_client.rectangle.getBoundsInLocal())) {
                            player_hit_block = true;
                        }
                    }
                    //endregion

                    //region If a collision happens - moves the player back
                    player_client.collision(player_hit_block, elapsedTime);
                    //endregion

                    //region Clears screen
                    graphicsContext.clearRect(0, 0, 800, 600);
                    //endregion

                    //region Renders all game objects
                    for (IRenderable go : objectManager.getGameObjects()) {
                        go.render(graphicsContext);
                    }
                    //endregion

//                  TODO: Send the client's bullets' positions & velocities to the server
                    Client.sendPacket(player_client.getPosition(), Client.getUniquePort());

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