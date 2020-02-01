package com.halflife3.View;

import com.halflife3.Controller.Input;
import com.halflife3.Controller.KeyHandle;
import com.halflife3.Model.Player;
import com.halflife3.Model.Vector2;
import com.sun.jdi.LongValue;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.Iterator;


public class Main extends Application {

    private Pane root = new Pane();

    private Player player = new Player(new Vector2(100, 100), new Vector2(0, 0), (short) 0);

    private class LongValue{
        public long value;
        public LongValue(long i){value = i;}
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
    //Try to use canvas instead of use the scene only

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

        KeyHandle handle = new KeyHandle();
        root.setOnKeyPressed(handle);
        root.setOnKeyReleased(handle);

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

//                briefcase.setVelocity(0, 0);
//                if (input.contains("LEFT"))
//                    briefcase.addVelocity(-50, 0);
//                if (input.contains("RIGHT"))
//                    briefcase.addVelocity(50, 0);
//                if (input.contains("UP"))
//                    briefcase.addVelocity(0, -50);
//                if (input.contains("DOWN"))
//                    briefcase.addVelocity(0, 50);
//
//                briefcase.update(elapsedTime);

// collision detection

//                Iterator<Sprite> moneybagIter = moneybagList.iterator();
//                while (moneybagIter.hasNext()) {
//                    Sprite moneybag = moneybagIter.next();
//                    if (briefcase.intersects(moneybag)) {
//                        moneybagIter.remove();
//                        score.value++;
//                    }
//                }

// render

                gc.clearRect(0, 0, 800, 600);
                player.render(gc);

//                for (Sprite moneybag : moneybagList)
//                    moneybag.render(gc);

                String pointsText = "Cash: $" + (100 * score.value);
                gc.fillText(pointsText, 360, 36);
                gc.strokeText(pointsText, 360, 36);
            }
        }.start();
        //main render.

        root.requestFocus();
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}

