package com.halflife3.GameUI.testUI;

import javafx.application.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.stage.*;

public class TestPressEsc extends Application {
    private boolean pause = false;
    @Override
    public void start(Stage stage) {

        //press button to enter pause:
        Button pauseButton = new Button("Pause");
        Pane pane = new Pane();
        pane.setMinSize(600, 400);
        BorderPane root = new BorderPane(pane, null, null, pauseButton, new Label(""+pause));
        //if esc is pressed
        root.setOnKeyPressed(keyEvent -> {
            if(keyEvent.getCode()==KeyCode.ESCAPE){
                pause = !pause;
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

                //actual stage of the pause menu
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
            }
        });

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) {

        Application.launch(args);
    }
}
