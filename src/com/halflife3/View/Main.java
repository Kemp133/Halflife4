package com.halflife3.View;

import com.halflife3.Controller.KeyHandle;
import com.halflife3.Controller.ObjectManager;
import com.halflife3.Model.Player;
import com.halflife3.Model.Vector2;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;


import java.io.FileInputStream;

import static javafx.scene.input.KeyCode.*;


public class Main extends Application {

    private Pane root = new Pane();
    private ObjectManager objectManager = new ObjectManager();

    private Player player = new Player(new Vector2(100, 100), new Vector2(0, 0), (short) 0, objectManager);

    //Translate the Gametime value format, will be used at timer part.
    private class LongValue {
        public long value;
        public LongValue(long i) {
            value = i;
        }
    }

    //Part of the camera.
    private double clampRange(double value, double min, double max) {
        if (value < min) return min ;
        if (value > max) return max ;
        return value ;
    }
    /**Initialize the root scene for the main game, and new game object can be
     * add by calling getChildren.add().
     * */
    //TODO: Build a Hashset to save all the Game Object or use Group? Group has some useful method for render
    private Parent createContent() {
        //root.setPrefSize(900, 400);

        //TODO: Use loop to add all the object into scene.
        //root.getChildren().add(player);
        return root;
    }
    /**Stage pass to start is the stage of game
     **/
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("HalfLife 3");
        Canvas canvas = new Canvas(800, 600);
        root.getChildren().add(canvas);
        Scene scene = new Scene(createContent(),800,600);
        primaryStage.setScene(scene);

        /**
         * Set the background
         * */
        FileInputStream inputted = new FileInputStream("res/background_image.png");
        Image image = new Image(inputted,40,40,true,true);
        BackgroundImage myBI= new BackgroundImage(image,
                BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT);
        root.setBackground(new Background(myBI));

        /**
         * Try to build a camera
         * */
        //root.getChildren().add(player.GetBounds());
        //Rectangle clip = new Rectangle(20,20,100,100);
        //Rectangle clip2 = new Rectangle(20,20,100,100);
        //canvas.setClip(clip);
        //root.setClip(clip);
        //canvas.translateXProperty().bind(clip.xProperty().multiply(-1));
        //canvas.translateYProperty().bind(clip.yProperty().multiply(-1));
        //Rectangle clip = new Rectangle(0,0,300,200);
//        clip.widthProperty().bind(scene.widthProperty());
//        clip.heightProperty().bind(scene.heightProperty());
//
//        clip.xProperty().bind(Bindings.createDoubleBinding(
//                () -> clampRange(player.getX() - scene.getWidth() / 2, 0, root.getWidth() - scene.getWidth()),
//                player.GetBounds().xProperty(), scene.widthProperty()));
//        clip.yProperty().bind(Bindings.createDoubleBinding(
//                () -> clampRange(player.getY() - scene.getHeight() / 2, 0, root.getHeight() - scene.getHeight()),
//                player.GetBounds().yProperty(), scene.heightProperty()));
        //root.setClip(clip);
        //root.translateXProperty().bind(clip.xProperty().multiply(-1));
        //root.translateYProperty().bind(clip.yProperty().multiply(-1));


        //set the key listener
        KeyHandle handle = new KeyHandle();
        root.setOnKeyPressed(handle);
        root.setOnKeyReleased(handle);

        //set the image for player, need to change the
        player.setImage("res/Player_pic.png");

        //Set the graphic tool for canvas
        GraphicsContext gc = canvas.getGraphicsContext2D();

        //main update.
        LongValue lastNanoTime = new LongValue(System.nanoTime());

        MapRender map = new MapRender();
        map.SetMap("res/map.png");
        map.loadLevel(objectManager);

        new AnimationTimer() {
            public void handle(long currentNanoTime) {
                // calculate time since last update.
                double elapsedTime = (currentNanoTime - lastNanoTime.value) / 1000000000.0;
                System.out.println(elapsedTime);
                lastNanoTime.value = currentNanoTime;

                // game logic
                if (handle.input.isKeyPressed(A))
                    //player.addVelocity(new Vector2(-100, 0));
                    player.getVelocity().setX(-100);
                else if (handle.input.isKeyPressed(D))
                    //player.addVelocity(new Vector2(100, 0));
                    player.getVelocity().setX(100);
                else if (handle.input.isKeyPressed(W))
                    //player.addVelocity(new Vector2(0, -100));
                    player.getVelocity().setY(-100);
                else if (handle.input.isKeyPressed(S))
                    //player.addVelocity(new Vector2(0, 100));
                    player.getVelocity().setY(100);
                else
                    player.getVelocity().reset();


                player.collision(map.get_list(),elapsedTime);
                player.update(elapsedTime);

                // render
                gc.clearRect(0, 0, 800, 600);
                player.render(gc);
                map.render(gc);
            }
        }.start();
        root.requestFocus();
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}