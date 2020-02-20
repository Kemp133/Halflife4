package com.halflife3.GameUI;/*This is the main menu after
log-in successfully to the database*/

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
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/*The class shows game manu after log-in successfully
* Function include:
* new game;load game
* audio setings*/
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

    private static final double SCREEN_WIDTH = 800;
    private static final double SCREEN_HEIGHT = 600;

    public void addMenu() throws IOException {
        main_menu.getStartItem().setOnAction(actionEvent -> {
            try {
                new ClientGame(this.getpStage()).getStarted();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        VBox manuBar = main_menu.getBar();
        manuBar.setAlignment(Pos.CENTER);
        pane.getChildren().add(manuBar);
    }

   public void addBackground() throws FileNotFoundException {
        FileInputStream inputStream = new FileInputStream("res/first_menu.png"); //change the backgraoud file plz
        Image image = new Image(inputStream);

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(SCREEN_WIDTH);
        imageView.setFitHeight(SCREEN_HEIGHT);

        Pane backgroud = new Pane(imageView);

        pane.getChildren().add(backgroud);
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
            primaryStage.setTitle("Team HalfLife");
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
