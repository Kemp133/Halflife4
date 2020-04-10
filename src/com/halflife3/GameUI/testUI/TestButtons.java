package com.halflife3.GameUI.testUI;

import javafx.application.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.*;


public class TestButtons extends Application {


    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Button Experiment 1");

        Text menu = new Text();
        menu.setText("game menu");
        //setting the position of the text
        menu.setX(100);
        menu.setY(50);
        Button button1 = new Button("Button 1");
        Button button2 = new Button("Button 2");
        Button button3 = new Button("Button 3");
        Button button4 = new Button("Button 4");

        button1.setStyle("-fx-border-color: #ff0000; -fx-border-width: 5px;");
        button2.setStyle("-fx-background-color: #00ff00");
        button3.setStyle("-fx-font-size: 2em; ");
        button4.setStyle("-fx-text-fill: #0000ff");


        VBox hbox = new VBox(button1, button2, button3, button4);
        Group group = new Group(menu,hbox);

        Scene scene = new Scene(group, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
