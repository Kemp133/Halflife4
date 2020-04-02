package com.halflife3.GameUI;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class MainMenu extends Application {
    BorderPane borderPane;
    VBox vbox;

    Button startServer = new Button("Host Game");
    Button joinGame = new Button("Join Game");
    Button leaderboard = new Button("Leaderboard");
    Button options = new Button("Options");
    Button exit = new Button("Exit");

    private Background addBackground() {
        try {

            FileInputStream inputStream = new FileInputStream("res/Leaderboard/LeaderboardBackground.jpg");
            Image image = new Image(inputStream);

            BackgroundSize backgroundSize = new BackgroundSize(800, 600, false, false, false, true);
            BackgroundImage backgroundImage = new BackgroundImage(image, BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, backgroundSize);
            Background background = new Background(backgroundImage);
            return background;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        return null;
    }

    private VBox vbox() {
        vbox = new VBox(startServer, joinGame, leaderboard, options, exit);
        vbox.setAlignment(Pos.BASELINE_CENTER);
        vbox.setPadding(new Insets(35, 0, 0, 30));
        vbox.setSpacing(30);
        vbox.setStyle("-fx-background-color: rgba(176,224,230,0.8);");

        return vbox;
    }

    private BorderPane borderPane() {
        borderPane = new BorderPane();

        Font paladinFont = null;
        try {
            paladinFont = Font.loadFont(new FileInputStream(new File("res/Font/PaladinsSemiItalic.otf")), 40);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Label title = new Label("Space Ball");
        title.setFont(paladinFont);
        title.setStyle("-fx-text-fill: linear-gradient(#0000FF, #FFFFFF 95%);");

        Insets insets  = new Insets(30);
        Pane topPane = new Pane();
        topPane.setMinSize(500, 100);
        Pane leftPane = new Pane();
        leftPane.setMinSize(100, 400);
        Pane rightPane = new Pane();
        rightPane.setMinSize(100, 400);
        Pane bottomPane = new Pane();
        bottomPane.setMinSize(500, 100);
        borderPane.setTop(title);
        BorderPane.setMargin(title, insets);
        borderPane.setAlignment(title, Pos.TOP_CENTER);

        borderPane.setLeft(leftPane);
        borderPane.setRight(rightPane);
        borderPane.setBottom(bottomPane);

        borderPane.setBackground(addBackground());
        borderPane.setCenter(vbox());

        File f = new File("res/com.halflife3.GameUI.MainMenu/MainMenuCSS.css");
        borderPane.getStylesheets().add("file:///" + f.getAbsolutePath().replace("\\", "/"));

        return borderPane;
    }

    @Override
    public void start(Stage stage) throws Exception {

        Scene scene = new Scene(borderPane(), 800, 600);

        /*File f = new File("res/com.halflife3.GameUI.MainMenu/MainMenuCSS.css");
        scene.getStylesheets().add("file:///" + f.getAbsolutePath().replace("\\", "/"));*/

        stage.setScene(scene);
        stage.show();
    }
}
