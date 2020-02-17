package com.halflife3.Networking.Server;
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
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.FileInputStream;
import static javafx.scene.input.KeyCode.*;


public class MainServer extends Application {
    static Input input = new Input();
    //static ServerPositionHandlerClient handler = new ServerPositionHandlerClient();
    private Pane root = new Pane();
    private ObjectManager objectManager = new ObjectManager();
    private Player player_client = new Player(new Vector2(100, 100), new Vector2(0, 0), (short) 0, objectManager,1);
    private Player player_server = new Player(new Vector2(300, 300), new Vector2(0, 0), (short) 0, objectManager,1);

    //private Vector2 client_position = player_client.getPosition();

/**
 * Plan to give each game_object an ID
 * And add every object to the object manager
 * 0: Block_Breakable
 * 1: Player
 * 2: Block_unBreakable
 * 3: Bullet
 * 4: Enemy
 * */


    public static void main(String[] args) {

        Server server = new Server();
        server.start();
        launch(args);
    }

    /**
     * Initialize the root scene for the main game, and new game object can be
     * add by calling getChildren.add().
     */
    //TODO: Build a Hashset to save all the Game Object or use Group? Group has some useful method for render
    private Parent createContent() {
        //root.setPrefSize(900, 400);
        //TODO: Use loop to add all the object into scene.
        //root.getChildren().add(player);
        return root;
    }

    /**
     * Stage pass to start is the stage of game
     **/
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("HalfLife 3");
        Canvas canvas = new Canvas(800, 600);
        root.getChildren().add(canvas);
        Scene scene = new Scene(createContent(), 800, 600);
        primaryStage.setScene(scene);

        /**
         * Set the background
         * */
        FileInputStream inputted = new FileInputStream("res/background_image.png");
        Image image = new Image(inputted, 40, 40, true, true);
        BackgroundImage myBI = new BackgroundImage(image,
                BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT);
        root.setBackground(new Background(myBI));

        /**
         * Try to build a camera
         * */
        //set the key listener
        KeyHandle handle = new KeyHandle();
        root.setOnKeyPressed(handle);
        root.setOnKeyReleased(handle);
        root.addEventHandler(MouseEvent.ANY, new MouseInput(input));
        scene.setCursor(Cursor.NONE);
        //Create cursor
        Crosshair cursor = new Crosshair(input.mousePosition, new Vector2(0, 0), (short) 0, objectManager, input,10);

        //set the image for player, need to change the
        player_client.setImage("res/Player_pic.png");
        player_server.setImage("res/Player_pic.png");

        //Set the graphic tool for canvas
        GraphicsContext gc = canvas.getGraphicsContext2D();

        //main update.
        final long[] startNanoTime = {System.nanoTime()};

        MapRender map = new MapRender(objectManager);
        map.SetMap("res/map.png");
        map.loadLevel();
        new AnimationTimer() {
            public void handle(long currentNanoTime) {

                // calculate time since last update.
                double elapsedTime = (currentNanoTime - startNanoTime[0]) / 1000000000.0;
                startNanoTime[0] = currentNanoTime;

                // game logic
                if (handle.input.isKeyPressed(A))
                    //player.addVelocity(new Vector2(-100, 0));
                    player_server.getVelocity().setX(-100);
                else if (handle.input.isKeyPressed(D))
                    //player.addVelocity(new Vector2(100, 0));
                    player_server.getVelocity().setX(100);
                else if (handle.input.isKeyPressed(W))
                    //player.addVelocity(new Vector2(0, -100));
                    player_server.getVelocity().setY(-100);
                else if (handle.input.isKeyPressed(S))
                    //player.addVelocity(new Vector2(0, 100));
                    player_server.getVelocity().setY(100);
                else
                    player_server.getVelocity().reset();
                if (input.isKeyPressed(C)) {
                    objectManager.getGameObjects().removeIf(go -> go.containsKey("Bullet"));
                }
                if (input.mouseButtonPressed.get(MouseButton.PRIMARY)) {
                    Bullet bullet = new Bullet(new Vector2(player_server.getX(), player_server.getY()), new Vector2(input.mousePosition.getX(), input.mousePosition.getY()).subtract(player_server.getPosition()), (short) 0, objectManager,3);
                }

                for(IUpdateable go : objectManager.getGameObjects()) {
                    go.update(elapsedTime);
                }
                //Put the collision detection into the main loop
                Boolean player_hit_block = false;
                for (Bricks block : map.get_list()) {
                    if (block.GetBounds().intersects(player_server.rectangle.getBoundsInLocal())) {
                        player_hit_block = true;
                        //this.velocity = new Vector2(0,0);
                    }
                }
                player_server.collision(player_hit_block, elapsedTime);
                //TODO: save client position and bullet position into a hashset, send single position atm
                //ServerPositionHandlerClient.setClient_position(player_client.getPosition());
                //player_server.setPosition(ServerPositionHandlerClient.getServer_position());


                // render
                gc.clearRect(0, 0, 800, 600);

                for (IRenderable go : objectManager.getGameObjects()) {
                    go.render(gc);
                }
            }
        }.start();
        root.requestFocus();
        primaryStage.show();

    }

}






