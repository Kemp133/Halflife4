package com.halflife3.View;

import com.halflife3.Controller.KeyHandle;
import com.halflife3.Model.Player;
import com.halflife3.Model.Vector2;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;


import static javafx.scene.input.KeyCode.*;


public class Main extends Application {

    private Pane root = new Pane();

    private Player player = new Player(new Vector2(100, 100), new Vector2(0, 0), (short) 0);

    //Translate the Gametime value format, will be used at timer part.
    private class LongValue {
        public long value;
        public LongValue(long i) {
            value = i;
        }
    }

    /*Initialize the root scene for the main game, and new game object can be
     * add by calling getChildren.add().
     * */
    //TODO: Build a Hashset to save all the Game Object or use Group? Group has some useful method for render
    private Parent createContent() {
        //root.setPrefSize(900, 400);
        //TODO: Use loop to add all the object into scene.
        //root.getChildren().add(player);
        return root;
    }
    /*
     * Stage pass to start is the stage of game
     * */
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("HalfLife 3");
        Scene scene = new Scene(createContent());
        primaryStage.setScene(scene);
        Canvas canvas = new Canvas(800, 600);
        root.getChildren().add(canvas);

        //set the key listener
        KeyHandle handle = new KeyHandle();
        root.setOnKeyPressed(handle);
        root.setOnKeyReleased(handle);

        //set the image for player, need to change the
        player.setImage("file:C:\\Users\\lenovo\\IdeaProjects\\halflife\\src\\com\\halflife3\\Model\\Player_pic.png");

        //Set the graphic tool for canvas
        GraphicsContext gc = canvas.getGraphicsContext2D();

        //main update.
        LongValue lastNanoTime = new LongValue(System.nanoTime());

        new AnimationTimer() {
            public void handle(long currentNanoTime) {
                // calculate time since last update.
                double elapsedTime = (currentNanoTime - lastNanoTime.value) / 1000000000.0;
                lastNanoTime.value = currentNanoTime;

                // game logic
                if (handle.input.isKeyPressed(A))
                    player.addVelocity(new Vector2(-100, 0));
                if (handle.input.isKeyPressed(D))
                    player.addVelocity(new Vector2(100, 0));
                if (handle.input.isKeyPressed(W))
                    player.addVelocity(new Vector2(0, -100));
                if (handle.input.isKeyPressed(S))
                    player.addVelocity(new Vector2(0, 100));

                player.update(elapsedTime);
                // TODO: collision detection

                // render
                gc.clearRect(0, 0, 800, 600);
                player.render(gc);
            }
        }.start();
        root.requestFocus();
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}

