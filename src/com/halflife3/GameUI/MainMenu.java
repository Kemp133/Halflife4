package com.halflife3.GameUI;

import com.halflife3.Controller.ClientController;
import com.halflife3.Controller.MapMenuController;
import com.halflife3.Controller.OptionsController;
import com.halflife3.Controller.SceneManager;
import com.halflife3.DatabaseUI.Leaderboard;
import com.halflife3.Networking.Server.Server;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.media.*;
import javafx.scene.text.Font;
import javafx.util.Duration;

/**
 * This class acts as a view for the MenuController, in the regards that it's purpose is to generate the menu and then allow the
 * MenuController to add any functionality it wants to the buttons that it contains. This class specificially deals with loading the main
 * menu for the game, and provides getters for the buttons that this contains so that the controller can set custom events for the buttons
 * based on what it needs them to do.
 */
public class MainMenu /*extends Application*/ {
	private       BorderPane borderPane;
	private       VBox       vbox;
	private final String     MENU_AUDIO_PATH                = "MainMenu/music_cinematic_darkness_falls.wav";
	private final String     MENU_BACKGROUND_IMAGE_LOCATION = "Leaderboard/LeaderboardBackground.jpg";

	private Button      startServer = new Button("Host Game");
	private Button      joinGame    = new Button("Join Game");
	private Button      leaderboard = new Button("Leaderboard");
	private Button      options     = new Button("Options");
	private Button      exit        = new Button("Exit");
	private Button      choose_map  = new Button("Maps");
	private MediaPlayer player;
	private Scene       scene;

	public MainMenu() {
		initialiseMenuScene();
	}

	private VBox vbox() {
		Media music = new Media(getClass().getClassLoader().getResource(MENU_AUDIO_PATH).toExternalForm());
		player = new MediaPlayer(music);
		player.play();

		joinGame.setOnAction(actionEvent -> {
			player.stop();
			Platform.runLater(() -> new ClientController().start(SceneManager.getInstance().getMainWindow()));
		});

		startServer.setOnAction(actionEvent -> {
			player.stop();
			Platform.runLater(() -> new Server().start());
			Platform.runLater(() -> new ClientController().start(SceneManager.getInstance().getMainWindow()));
		});

		choose_map.setOnAction(actionEvent -> {
			player.stop();
//			MapMenuController map_control = new MapMenuController();
			Platform.runLater(() -> new MapMenuController().start());
		});

		leaderboard.setOnAction(actionEvent -> {
			player.stop();
			Leaderboard lb = new Leaderboard();
			SceneManager.getInstance().setScene("Leader Board", lb.returnScene());
		});

		options.setOnAction(actionEvent -> {
			Platform.runLater(() -> new OptionsController(player).start());
		});

		exit.setOnAction(actionEvent -> {
			Platform.exit();
			System.exit(0); //This is done to actually stop the Java application that's running
		});

		vbox = new VBox(startServer, joinGame, choose_map, leaderboard, options, exit);
		vbox.setAlignment(Pos.BASELINE_CENTER);
		vbox.setPadding(new Insets(20, 0, 0, 30));
		vbox.setSpacing(25);
		vbox.setStyle("-fx-background-color: rgba(176,224,230,0.8);");

		return vbox;
	}

	private BorderPane borderPane() {
		borderPane = new BorderPane();

		Font paladinFont = Font.loadFont(getClass().getClassLoader().getResourceAsStream("Font/PaladinsSemiItalic.otf"), 40);

		Label title = new Label("Space Ball");
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

		borderPane.setBackground(MenuUtilitites.getBackground(getClass(), MENU_BACKGROUND_IMAGE_LOCATION));
		borderPane.setCenter(vbox());

		borderPane.getStylesheets().add(getClass().getClassLoader().getResource("MainMenu/MainMenuCSS.css").toExternalForm());

		return borderPane;
	}

	private void initialiseMenuScene() {
		scene = new Scene(borderPane(), 800, 600);
		configureMediaPlayer();
	}

	private void configureMediaPlayer() {
		player.setVolume(0.1);
		player.setOnEndOfMedia(() -> player.seek(Duration.ZERO));
		player.setAutoPlay(true);
	}

	public Scene getScene()        { return scene; }
	public MediaPlayer getPlayer() { return player; }
	public Button getExit()        { return exit; }
}