package com.halflife3.GameUI;

import com.halflife3.Controller.ClientController;
import com.halflife3.Controller.SceneManager;
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

/*The class shows game menu after log-in successfully
 * Function include:
 * new game
 * load game
 * audio settings*/
public class Windows extends Application {
	private StackPane pane;
	private LinMenu   main_menu = new LinMenu();
	private Stage     pStage    = new Stage();
	private SceneManager manager;

	public Windows (SceneManager manager) throws FileNotFoundException {
	    this.manager = manager;
    } //This needs to be here, can't be helped

	@Override
	public void start (Stage primaryStage) {
		try {
			pane = new StackPane();
			setPStage(primaryStage);
			primaryStage.setTitle("Team HalfLife : Man in black");
			primaryStage.setResizable(false);
			primaryStage.setMaxHeight(FirstMenu.SCREEN_HEIGHT);
			primaryStage.setMaxWidth(FirstMenu.SCREEN_WIDTH);
			Scene scene = new Scene(createContent(), FirstMenu.SCREEN_WIDTH, FirstMenu.SCREEN_HEIGHT, Color.WHITE);
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

	public StackPane createContent () throws FileNotFoundException {
		addBackground();
		addMenu();

		return pane;
	}

	public void addBackground () throws FileNotFoundException {
		var inputStream = new FileInputStream("res/MenuBackground.png");
		var image       = new Image(inputStream);
		var imageView   = new ImageView(image);
		imageView.setFitWidth(FirstMenu.SCREEN_WIDTH);
		imageView.setFitHeight(FirstMenu.SCREEN_HEIGHT);
		var background = new Pane(imageView);
		pane.getChildren().add(background);
	}

	public void addMenu () {
		main_menu.startGameButton().setOnAction(actionEvent -> {
			try {
				main_menu.player.stop();
				new ClientGame(getPStage()).getStarted();
			} catch (Exception e) { e.printStackTrace(); }
		});

		VBox menuBar = main_menu.getBar();
		menuBar.setAlignment(Pos.CENTER);
		pane.getChildren().add(menuBar);
	}

	public Stage getPStage () {
		try {
			return pStage;
		} catch (Exception e) { System.err.println("Primary stage not built!"); }

		return null;
	}

	public void setPStage (Stage stage) {
		pStage = stage;
	}

	public static Scene getMenuScene (SceneManager manager) {
		StackPane pane = new StackPane();

		try {
		LinMenu menu = new LinMenu();
			var inputStream = new FileInputStream("res/MenuBackground.png");
			var image       = new Image(inputStream);
			var imageView   = new ImageView(image);
			imageView.setFitWidth(FirstMenu.SCREEN_WIDTH);
			imageView.setFitHeight(FirstMenu.SCREEN_HEIGHT);
			var background = new Pane(imageView);
			pane.getChildren().add(background);

			menu.startGameButton().setOnAction(actionEvent -> {
                try {
                    menu.player.stop();
                    new ClientController().run(manager);
                } catch (Exception e) { e.printStackTrace(); }
			});

			VBox menuBar = LinMenu.getMenuVBox();
			menuBar.setAlignment(Pos.CENTER);
			pane.getChildren().add(menuBar);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new Scene(pane, FirstMenu.SCREEN_WIDTH, FirstMenu.SCREEN_HEIGHT, Color.WHITE);
	}
}
