package com.halflife3.GameUI;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;

public class PauseMenu extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        //Scene width
        int sceneWidth = 400;
        String menuName = "GAME MENU";
        stage.setTitle(menuName);
        //Setting the menu text
        Text menu = new Text();
        menu.setText(menuName);
        //setting the position of the text
        menu.setX((double)sceneWidth/2);
        menu.setY(50);
        //Create buttons:
        Button returnToGame = new Button("Return to game");
        Button options = new Button("Options");
        Button toMainMenu = new Button("Quit to Main Menu");
        Button toDesktop = new Button("Quit to Desktop");
        //edit button properties:
        returnToGame.setStyle("-fx-font-size: 2em; ");
        options.setStyle("-fx-font-size: 2em; ");
        toMainMenu.setStyle("-fx-font-size: 2em; ");
        toDesktop.setStyle("-fx-font-size: 2em; ");
        //We want buttons in a vertical row, so we use vbox
        VBox vbox = new VBox(returnToGame,options,toMainMenu,toDesktop);
        //a group for both text and buttons
        Group group = new Group(menu,vbox);
        Scene scene = new Scene(group, sceneWidth, 400);
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) {
        Application.launch(args);
    }
}
