package com.halflife3.GameUI;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import javafx.stage.StageStyle;

public class PauseMenu extends Application {
    @Override
    public void start(Stage stage) throws Exception {

        //press button to enter pause:
        Button pauseButton = new Button("Pause");
        Pane pane = new Pane();
        pane.setMinSize(600, 400);
        BorderPane root = new BorderPane(pane, null, null, pauseButton, new Label("This is\nthe test scene"));

        //when button is pressed
        pauseButton.setOnAction(e -> {
            //set background effect
            root.setEffect(new GaussianBlur());
            //setting up vbox for buttons
            //we will have 1 line of description with 4 buttons, that 5 rows
            VBox pauseRoot = new VBox(5);
            pauseRoot.setStyle("-fx-background-color: rgba(255, 255, 255, 0.8);");
            pauseRoot.setAlignment(Pos.CENTER);
            pauseRoot.setPadding(new Insets(20));
            //actual stuff in the rows
            pauseRoot.getChildren().add(new Label("Paused"));
            //Button 1
            Button returnToGame = new Button("Return to game");
            pauseRoot.getChildren().add(returnToGame);
            //Button 2
            Button options = new Button("Options");
            pauseRoot.getChildren().add(options);
            //Button 3
            Button toMainMenu = new Button("Quit to Main Menu");
            pauseRoot.getChildren().add(toMainMenu);
            //Button 4
            Button toDesktop = new Button("Quit to Desktop");
            pauseRoot.getChildren().add(toDesktop);

            Stage popupStage = new Stage(StageStyle.TRANSPARENT);
            popupStage.initOwner(stage);
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setScene(new Scene(pauseRoot, Color.TRANSPARENT));
            //Button function 1:resume
            returnToGame.setOnAction(event-> {
                root.setEffect(null);
                popupStage.hide();
                    });
            popupStage.show();
                });
        //stats of the pause button
        BorderPane.setAlignment(pauseButton, Pos.CENTER);
        BorderPane.setMargin(pauseButton, new Insets(5));

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) {

        Application.launch(args);
    }
    /*
    //buttons that need to be in pause
    public VBox pauseVBox(){
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
        return new VBox(returnToGame,options,toMainMenu,toDesktop);
    }
    public void pauseActions(BorderPane bdpane, VBox vbox){

    }*/
}
