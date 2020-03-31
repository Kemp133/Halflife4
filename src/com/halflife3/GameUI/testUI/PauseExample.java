package com.halflife3.GameUI.testUI;

import javafx.animation.Animation;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class PauseExample extends Application {

    @Override
    public void start(Stage primaryStage) {

        Rectangle rect = new Rectangle(50, 50, 50, 50);
        rect.setFill(Color.CORAL);

        TranslateTransition animation = createAnimation(rect);

        Button pauseButton = new Button("Pause");

        Pane pane = new Pane(rect);
        pane.setMinSize(600, 150);

        BorderPane root = new BorderPane(pane, null, null, pauseButton, new Label("This is\nthe main\nscene"));

        pauseButton.setOnAction(e -> {
            animation.pause();
            root.setEffect(new GaussianBlur());

            VBox pauseRoot = new VBox(5);
            pauseRoot.getChildren().add(new Label("Paused"));
            pauseRoot.setStyle("-fx-background-color: rgba(255, 255, 255, 0.8);");
            pauseRoot.setAlignment(Pos.CENTER);
            pauseRoot.setPadding(new Insets(20));

            Button resume = new Button("Resume");
            pauseRoot.getChildren().add(resume);

            Stage popupStage = new Stage(StageStyle.TRANSPARENT);
            popupStage.initOwner(primaryStage);
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setScene(new Scene(pauseRoot, Color.TRANSPARENT));


            resume.setOnAction(event -> {
                root.setEffect(null);
                animation.play();
                popupStage.hide();
            });

            popupStage.show();
        });

        BorderPane.setAlignment(pauseButton, Pos.CENTER);
        BorderPane.setMargin(pauseButton, new Insets(5));
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private TranslateTransition createAnimation(Rectangle rect) {
        TranslateTransition animation = new TranslateTransition(Duration.seconds(1), rect);
        animation.setByX(400);
        animation.setCycleCount(Animation.INDEFINITE);
        animation.setAutoReverse(true);
        animation.play();
        return animation;
    }

    public static void main(String[] args) {
        launch(args);
    }
}