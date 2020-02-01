package com.halflife3.View;

import com.halflife3.Controller.KeyHandle;
import com.halflife3.Model.Player;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;


public class Main extends Application {

    private Pane root = new Pane();
    private Player player = new Player();

    /*Initialize the root scene for the main game, and new game object can be
    * add by calling getChildren.add().
    * */
    //TODO: Build a Hashset to save all the Game Object or use Group? Group has some useful method for render
    private Parent createContent(){
        root.setPrefSize(900, 400);
        //TODO: Use loop to add all the object into scene.
        root.getChildren().add(player);
        return root;
    }

    /*
    * Stage pass to start is the stage of game
    * */
    @Override
    public void start(Stage primaryStage) throws Exception{
        primaryStage.setTitle("HalfLife 3");
        Scene scene = new Scene(createContent());

        KeyHandle handle = new KeyHandle();
        root.setOnKeyPressed(handle);

        primaryStage.setScene(scene);
        root.requestFocus();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

