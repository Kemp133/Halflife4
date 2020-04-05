package com.halflife3.GameUI;

import com.halflife3.Controller.ClientController;
import com.halflife3.Controller.MapMenuController;
import com.halflife3.Controller.SceneManager;
import com.halflife3.Networking.NetworkingUtilities;
import com.halflife3.Networking.Server.Server;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Maps {
    static public  String      Map = "res/map.png";
    private BorderPane  borderPane;
    private VBox        vbox;
    private String      choice = "Maps";
    private Button      map1    = new Button("Map1");
    private Button      map2    = new Button("Map2");
    private Button      map3    = new Button("Map3");
    private Button      back    = new Button("Back to Main Menu");
    private ImageView   imgView1;
    private ImageView   imgView2;
    private ImageView   imgView3;


    private Scene scene;

    public Maps() {
        initialiseMenuScene();
    }

    private VBox vbox(){

        map1.setOnAction(actionEvent -> {
            Map = "res/map1.png";
            map1.getStyleClass().add("button1");
            map2.getStyleClass().remove("button1");
            map3.getStyleClass().remove("button1");
        });



        map2.setOnAction(actionEvent -> {
            Map = "res/map2.png";
            map2.getStyleClass().add("button1");
            map1.getStyleClass().remove("button1");
            map3.getStyleClass().remove("button1");
        });


        map3.setOnAction(actionEvent -> {
            Map = "res/map3.png";
            map3.getStyleClass().add("button1");
            map1.getStyleClass().remove("button1");
            map2.getStyleClass().remove("button1");
        });
        getImage();
        HBox h1 = new HBox(map1, imgView1);
        HBox h2 = new HBox(map2, imgView2);
        HBox h3 = new HBox(map3, imgView3);
        h1.setAlignment(Pos.BASELINE_CENTER);
        h2.setAlignment(Pos.BASELINE_CENTER);
        h3.setAlignment(Pos.BASELINE_CENTER);
        h1.setSpacing(40);
        h2.setSpacing(40);
        h3.setSpacing(40);

        back.setOnAction(actionEvent -> {
            MapMenuController map_control = new MapMenuController();
            map_control.end();
        });

        vbox = new VBox(h1,h2, h3, back);
        vbox.setAlignment(Pos.BASELINE_CENTER);
        vbox.setPadding(new Insets(30, 0, 0, 20));
        vbox.setSpacing(20);
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

        Label title = new Label(choice);
        title.setFont(paladinFont);
        title.setStyle("-fx-text-fill: linear-gradient(#0000FF, #FFFFFF 95%);");

        Insets insets  = new Insets(30);
        Pane   topPane = new Pane();
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

        borderPane.setBackground(MenuUtilitites.getBackground(getClass(), "res/Leaderboard/LeaderboardBackground.jpg"));
        borderPane.setCenter(vbox());

        File f = new File("res/MainMenu/MainMenuCSS.css");
        borderPane.getStylesheets().add("file:///" + f.getAbsolutePath().replace("\\", "/"));
        return borderPane;
    }

    private void initialiseMenuScene() {
        scene = new Scene(borderPane(), 800, 600);
    }

    private void getImage(){
        try {
            Image mapImage1 = new Image(new FileInputStream("res/map1.png"));
            Image mapImage2 = new Image(new FileInputStream("res/map2.png"));
            Image mapImage3 = new Image(new FileInputStream("res/map3.png"));
            imgView1 = new ImageView(mapImage1);
            imgView2 = new ImageView(mapImage2);
            imgView3 = new ImageView(mapImage3);
            imgView1.setFitHeight(100);
            imgView1.setFitWidth(220);
            imgView2.setFitHeight(100);
            imgView2.setFitWidth(220);
            imgView3.setFitHeight(100);
            imgView3.setFitWidth(220);

        }catch (IOException e){
            System.out.println("Can't find Image.");
        }
    }

    public Scene getScene()        { return scene; }

    public Button getBack()        { return back; }
}
