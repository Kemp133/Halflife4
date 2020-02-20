package com.halflife3.GameUI;

import com.halflife3.Networking.Client.ClientGame;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/*The class shows game menu after log-in successfully
* Function include:
* new game;load game
* audio settings*/
public class Windows extends Application {

    private StackPane pane;
    public StackPane getPane(){
        return this.pane;
    }
    private LinMenu main_menu = new LinMenu();
    private Stage pStage = new Stage();

    public Windows() throws FileNotFoundException {
        String test = "This is a test";
    }

    private static final double SCREEN_WIDTH = Screen.getPrimary().getBounds().getWidth();
    private static final double SCREEN_HEIGHT = Screen.getPrimary().getBounds().getHeight();

    public void addMenu() throws IOException {
        main_menu.getStartItem().setOnAction(actionEvent -> {
            try {
                main_menu.player.stop();
                new ClientGame(this.getpStage()).getStarted();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        VBox menuBar = main_menu.getBar();
        menuBar.setAlignment(Pos.CENTER);
        pane.getChildren().add(menuBar);
    }

   public void addBackground() throws FileNotFoundException {
        FileInputStream inputStream = new FileInputStream("res/MenuBackground.png");
        Image image = new Image(inputStream);

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(800);
        imageView.setFitHeight(600);

        Pane background = new Pane(imageView);

        pane.getChildren().add(background);
    }

    public StackPane createContent() throws FileNotFoundException {
        this.addBackground();
        try {
            this.addMenu();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this.pane;
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            pane = new StackPane();
            setpStage(primaryStage);
            primaryStage.setTitle("Team HalfLife : Man in black");
            primaryStage.setResizable(false);
            primaryStage.setMaxHeight(600);
            primaryStage.setMaxWidth(800);
            Scene scene = new Scene(this.createContent(), SCREEN_WIDTH, SCREEN_HEIGHT, Color.WHITE);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    public Stage getpStage() {
        try {
            return this.pStage;
        } catch (Exception e) {
            System.err.println("primary stage not built!");
        }
        return null;
    }

    public void setpStage(Stage stage) {
        pStage = stage;
    }

}
