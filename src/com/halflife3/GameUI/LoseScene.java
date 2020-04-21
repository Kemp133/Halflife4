package com.halflife3.GameUI;

import com.halflife3.Controller.MenuController;
import com.halflife3.Controller.SceneManager;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * This class acts as a view for the Final stage of the game, if you win the game you will see this page.
 */
public class LoseScene /*extends Application*/ {
	private              VBox   vbox;
	private static final String MENU_BACKGROUND_IMAGE_LOCATION = "Leaderboard/LeaderboardBackground.jpg";
	private static final String Lose_pic                       = "numbers/lose.png";
	private              Button exit                           = new Button("Back");
	private              Scene  scene;

	public LoseScene() {
		initialiseMenuScene();
	}

	private VBox vbox() {
		exit.setOnAction(actionEvent -> {
			MainMenu main = new MainMenu();
			SceneManager.getInstance().setScene("Main Menu", main.getScene());
		});

		VBox vbox2 = new VBox(exit);
		vbox2.setAlignment(Pos.CENTER);
		vbox2.setPadding(new Insets(50, 0, 0, 0));

		Image     win_image = new Image(getClass().getClassLoader().getResourceAsStream(Lose_pic));
		ImageView Win_show  = new ImageView(win_image);
		vbox = new VBox(Win_show, vbox2);

		vbox.setAlignment(Pos.BASELINE_CENTER);
		vbox.setPadding(new Insets(120, 0, 40, 0));

		return vbox;
	}

	private BorderPane borderPane() {
		BorderPane borderPane = new BorderPane();

		Font paladinFont = Font.loadFont(getClass().getClassLoader().getResourceAsStream("Font/PaladinsSemiItalic.otf"), 40);

		Label title = new Label("Space Ball");
		title.setFont(paladinFont);
		title.setStyle("-fx-text-fill: linear-gradient(#0000FF, #FFFFFF 95%);");

		Insets insets  = new Insets(30);
		Pane   topPane = new Pane();
		topPane.setMinSize(500, 100);
		Pane leftPane = new Pane();
		leftPane.setMinSize(0, 0);
		Pane rightPane = new Pane();
		rightPane.setMinSize(0, 0);
		Pane bottomPane = new Pane();
		bottomPane.setMinSize(500, 100);
		borderPane.setTop(title);
		BorderPane.setMargin(title, insets);
		BorderPane.setAlignment(title, Pos.TOP_CENTER);

		borderPane.setLeft(leftPane);
		borderPane.setRight(rightPane);
		borderPane.setBottom(bottomPane);

		borderPane.setBackground(MenuUtilitites.getBackground(getClass(), MENU_BACKGROUND_IMAGE_LOCATION));
		borderPane.setCenter(vbox());

		borderPane.getStylesheets().add(getClass().getClassLoader().getResource("MainMenu/MainMenuCSS.css").toExternalForm());

		return borderPane;
	}

	private void initialiseMenuScene() {
		scene = new Scene(borderPane(), WindowAttributes.GAME_WINDOW_WIDTH, WindowAttributes.GAME_WINDOW_HEIGHT);
	}

	public Scene getScene() {
		return scene;
	}
}

