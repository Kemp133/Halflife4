package com.halflife3.GameUI;

import com.halflife3.Controller.ClientController;
import com.halflife3.Controller.Exceptions.SceneDoesNotExistException;
import com.halflife3.Controller.Exceptions.SceneStackEmptyException;
import com.halflife3.Controller.MapMenuController;
import com.halflife3.Controller.SceneManager;
import com.halflife3.DatabaseUI.Leaderboard;
import com.halflife3.DatabaseUI.SettingsMenu;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.module.FindException;

/**
 * This class acts as a view for the Final stage of the game, if you win the game you will see this page.
 */
public class WinScene /*extends Application*/ {
    private BorderPane borderPane;
    private VBox vbox;
    private HBox hbox;
    private static final String MENU_BACKGROUND_IMAGE_LOCATION = "res/Leaderboard/LeaderboardBackground.jpg";
    private static final String Win_pic = "res/numbers/win.png";
    private Button exit = new Button("Back");
    private Scene scene;

    public WinScene() {
        initialiseMenuScene();
    }

    private VBox vbox() {
        exit.setOnAction(actionEvent -> {
            MainMenu main = new MainMenu();
            SceneManager.getInstance().setScene("Main Menu", main.getScene());
        });
        VBox vbox2 = new VBox(exit);
        vbox2.setAlignment(Pos.CENTER);
        vbox2.setPadding(new Insets(50,0,0,0));
        try{
            Image win_image = new Image(new FileInputStream(Win_pic));
            ImageView Win_show = new ImageView(win_image);
            vbox = new VBox(Win_show,vbox2);
        }catch (FileNotFoundException e){
            System.out.println("Image not found");
        }
        vbox.setAlignment(Pos.BASELINE_CENTER);
        vbox.setPadding(new Insets(120, 0, 40, 0));
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

        Insets insets = new Insets(30);
        Pane topPane = new Pane();
        topPane.setMinSize(500, 100);
        Pane leftPane = new Pane();
        leftPane.setMinSize(0, 0);
        Pane rightPane = new Pane();
        rightPane.setMinSize(0, 0);
        Pane bottomPane = new Pane();
        bottomPane.setMinSize(500, 100);
        borderPane.setTop(title);
        BorderPane.setMargin(title, insets);
        borderPane.setAlignment(title, Pos.TOP_CENTER);

        borderPane.setLeft(leftPane);
        borderPane.setRight(rightPane);
        borderPane.setBottom(bottomPane);

        borderPane.setBackground(MenuUtilitites.getBackground(getClass(), MENU_BACKGROUND_IMAGE_LOCATION));
        borderPane.setCenter(vbox());

        File f = new File("res/MainMenu/MainMenuCSS.css");
        borderPane.getStylesheets().add("file:///" + f.getAbsolutePath().replace("\\", "/"));

        return borderPane;
    }

    private void initialiseMenuScene() {
        scene = new Scene(borderPane(), 800, 600);
    }

    public Scene getScene() {
        return scene;
    }
}

