//package com.halflife3.Networking.Server;
//
//import com.halflife3.Controller.Input;
//import com.halflife3.Controller.KeyHandle;
//import com.halflife3.Controller.MouseInput;
//import com.halflife3.Controller.ObjectManager;
//import com.halflife3.Model.*;
//import com.halflife3.View.Main;
//import com.halflife3.View.MapRender;
//import javafx.animation.AnimationTimer;
//import javafx.application.Application;
//import javafx.scene.Cursor;
//import javafx.scene.canvas.Canvas;
//import javafx.scene.canvas.GraphicsContext;
//import javafx.scene.Parent;
//import javafx.scene.Scene;
//import javafx.scene.image.Image;
//import javafx.scene.input.MouseButton;
//import javafx.scene.input.MouseEvent;
//import javafx.scene.layout.*;
//import javafx.stage.Stage;
//
//import java.io.FileInputStream;
//
//import static javafx.scene.input.KeyCode.*;
//
//
//public class MainServer extends Application {
//    static Input input = new Input();
//    private Pane root = new Pane();
//    private ObjectManager objectManager = new ObjectManager();
//    private Player player = new Player(new Vector2(100, 100), new Vector2(0, 0), (short) 0, objectManager);
//
//    public static void main(String[] args) {
//
//        Server server = new Server();
//        server.start();
//        launch(args);
//    }
//
//    /**
//     * Initialize the root scene for the main game, and new game object can be
//     * add by calling getChildren.add().
//     */
//    //TODO: Build a Hashset to save all the Game Object or use Group? Group has some useful method for render
//    private Parent createContent() {
//        //root.setPrefSize(900, 400);
//        //TODO: Use loop to add all the object into scene.
//        //root.getChildren().add(player);
//        return root;
//    }
//
//    /**
//     * Stage pass to start is the stage of game
//     **/
//    @Override
//    public void start(Stage primaryStage) throws Exception {
//        primaryStage.setTitle("HalfLife 3");
//        Canvas canvas = new Canvas(800, 600);
//        root.getChildren().add(canvas);
//        Scene scene = new Scene(createContent(), 800, 600);
//        primaryStage.setScene(scene);
//
//        /**
//         * Set the background
//         * */
//        FileInputStream inputted = new FileInputStream("res/background_image.png");
//        Image image = new Image(inputted, 40, 40, true, true);
//        BackgroundImage myBI = new BackgroundImage(image,
//                BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.DEFAULT,
//                BackgroundSize.DEFAULT);
//        root.setBackground(new Background(myBI));
//
//        /**
//         * Try to build a camera
//         * */
//        //set the key listener
//        KeyHandle handle = new KeyHandle();
//        root.setOnKeyPressed(handle);
//        root.setOnKeyReleased(handle);
//        root.addEventHandler(MouseEvent.ANY, new MouseInput(input));
//        scene.setCursor(Cursor.NONE);
//        //Create cursor
//        Crosshair cursor = new Crosshair(input.mousePosition, new Vector2(0, 0), (short) 0, objectManager, input);
//
//        //set the image for player, need to change the
//        player.setImage("res/Player_pic.png");
//
//        //Set the graphic tool for canvas
//        GraphicsContext gc = canvas.getGraphicsContext2D();
//
//        //main update.
//        com.halflife3.Networking.Server.MainServer.LongValue lastNanoTime = new com.halflife3.Networking.Server.MainServer.LongValue(System.nanoTime());
//
//        MapRender map = new MapRender();
//        map.SetMap("res/map.png");
//        map.loadLevel(objectManager);
//        new AnimationTimer() {
//            public void handle(long currentNanoTime) {
//
//                // calculate time since last update.
//                double elapsedTime = (currentNanoTime - lastNanoTime.value) / 1000000000.0;
//                //System.out.println(elapsedTime);
//                lastNanoTime.value = currentNanoTime;
//
//                // game logic
//                if (handle.input.isKeyPressed(A))
//                    //player.addVelocity(new Vector2(-100, 0));
//                    player.getVelocity().setX(-100);
//                else if (handle.input.isKeyPressed(D))
//                    //player.addVelocity(new Vector2(100, 0));
//                    player.getVelocity().setX(100);
//                else if (handle.input.isKeyPressed(W))
//                    //player.addVelocity(new Vector2(0, -100));
//                    player.getVelocity().setY(-100);
//                else if (handle.input.isKeyPressed(S))
//                    //player.addVelocity(new Vector2(0, 100));
//                    player.getVelocity().setY(100);
//                else
//                    player.getVelocity().reset();
//                if (input.isKeyPressed(C)) {
//                    objectManager.getGameObjects().removeIf(go -> go.containsKey("Bullet"));
//                }
//                if (input.mouseButtonPressed.get(MouseButton.PRIMARY)) {
//                    Bullet bullet = new Bullet(new Vector2(player.getX(), player.getY()), new Vector2(input.mousePosition.getX(), input.mousePosition.getY()).subtract(player.getPosition()), (short) 0, objectManager);
//                }
//
//
//                player.update(elapsedTime);
//                //Put the collision detection into the main loop
//                Boolean player_hit_block = false;
//                for (Bricks block : map.get_list()) {
//                    if (block.GetBounds().intersects(player.rectangle.getBoundsInLocal())) {
//                        player_hit_block = true;
//                        //this.velocity = new Vector2(0,0);
//                    }
//                }
//                player.collision(player_hit_block, elapsedTime);
//
//
//                // render
//                gc.clearRect(0, 0, 800, 600);
//                player.render(gc);
//                map.render(gc);
//                for (IRenderable go : objectManager.getGameObjects()) {
//                    go.render(gc);
//                }
//            }
//        }.start();
//        root.requestFocus();
//        primaryStage.show();
//
//    }
//
//    //Translate the Gametime value format, will be used at timer part.
//    private class LongValue {
//        public long value;
//
//        public LongValue(long i) {
//            value = i;
//        }
//    }
//}
//
//
//
//
