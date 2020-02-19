//package com.halflife3.View;
//
//import com.halflife3.Controller.Input;
//import com.halflife3.Controller.KeyHandle;
//import com.halflife3.Controller.MouseInput;
//import com.halflife3.Controller.ObjectManager;
//import com.halflife3.Model.*;
//import com.halflife3.Model.Interfaces.IRenderable;
//import javafx.animation.AnimationTimer;
//import javafx.application.Application;
//import javafx.scene.Cursor;
//import javafx.scene.Parent;
//import javafx.scene.Scene;
//import javafx.scene.canvas.Canvas;
//import javafx.scene.canvas.GraphicsContext;
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
//public class Main extends Application {
//    private Pane root = new Pane();
//    private ObjectManager objectManager = new ObjectManager();
//    private Player player = new Player(new Vector2(100, 100), new Vector2(0, 0), (short) 0, objectManager);
//
//    private MeleeEnemy melee = new MeleeEnemy(new Vector2(150,150), new Vector2(0, 0),(short) 0 ,objectManager);
//
//    static Input input = new Input();
//
//    //Translate the Gametime value format, will be used at timer part.
//    private static class LongValue {
//        public long value;
//
//        public LongValue(long i) {
//            value = i;
//        }
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
//        /*
//          Set the background
//         */
//        FileInputStream inputted = new FileInputStream("res/background_image.png");
//        Image image = new Image(inputted, 40, 40, true, true);
//        BackgroundImage myBI = new BackgroundImage(image,
//                BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.DEFAULT,
//                BackgroundSize.DEFAULT);
//        root.setBackground(new Background(myBI));
//
//        /*
//          Try to build a camera
//          */
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
//        //set the image for meleeEnemy
//        melee.setImage("res/pixil-frame-0.png");
//
//        //Set the graphic tool for canvas
//        GraphicsContext gc = canvas.getGraphicsContext2D();
//
//        //main update.
//        LongValue lastNanoTime = new LongValue(System.nanoTime());
//
//        MapRender map = new MapRender(objectManager);
//        map.SetMap("res/mapAndEnemy.png");
//        map.loadLevel();
//        new AnimationTimer() {
//            public void handle(long currentNanoTime) {
//
//
//                // calculate time since last update.
//                double elapsedTime = (currentNanoTime - lastNanoTime.value) / 1000000000.0;
//                System.out.println(elapsedTime);
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
//                boolean player_hit_block = false;
//                for (Bricks block : map.get_list()) {
//                    if (block.GetBounds().intersects(player.rectangle.getBoundsInLocal())) {
//                        player_hit_block = true;
//                        //this.velocity = new Vector2(0,0);
//                    }
//                }
//                player.collision(player_hit_block, elapsedTime);
//
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
//
//        root.requestFocus();
//        primaryStage.show();
//
//    }
//
//    public static void main(String[] args) {
//        launch(args);
//
//    }
//}