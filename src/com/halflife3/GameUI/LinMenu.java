package com.halflife3.GameUI;

import com.halflife3.Controller.ClientController;
import com.halflife3.Controller.SceneManager;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class LinMenu extends ContextMenu {
	VBox main_menu;
	MediaPlayer    player;
	Slider         volume;
	MenuItem       audioItem_on;
	MenuItem       audioItem_off;
	CustomMenuItem audio;
	Button         start_m;
	MenuButton     settings_m;
	Button         exit_m;

	public LinMenu () throws FileNotFoundException {
		//region Audio Settings
		settings_m = new MenuButton("", new ImageView(), audioItem_on, audioItem_off, audio);
		settings_m.setMinHeight(90);
		settings_m.setMinWidth(140);
		var inputAudio = new FileInputStream("res/audiogamebuttongimp.png");
		var imageAudio = new Image(inputAudio);
		var bImageAudio = new BackgroundImage(imageAudio,
				BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
				new BackgroundSize(settings_m.getWidth(), settings_m.getHeight(), true, true, true, false));
		var newBAudio = new Background(bImageAudio);
		settings_m.setBackground(newBAudio);

		File audio_path = new File("res/MenuMusic.mp3");
		volume = new Slider();
		Media media = new Media(audio_path.toURI().toString());
		player        = new MediaPlayer(media);
		audioItem_on  = new MenuItem("Audio On");
		audioItem_off = new MenuItem("Audio Off");
		audio         = new CustomMenuItem(volume);
		audio.setHideOnClick(false);

		audioItem_on.setOnAction(actionEvent -> player.setMute(false));
		audioItem_off.setOnAction(actionEvent -> player.setMute(true));
		audio.setOnAction(actionEvent -> volumeControl());

		player.setAutoPlay(true);
		player.setOnEndOfMedia(() -> {
			player.seek(Duration.ZERO);
			player.play();
		});
		player.setOnError(() -> System.err.println("Error in playing bgm"));
		//endregion

		//region Play button
		start_m = new Button();
		start_m.setMinHeight(90);
		start_m.setMinWidth(140);
		var inputJoin = new FileInputStream("res/joingamebuttongimp.png");
		var imageJoin = new Image(inputJoin);
		var bImageJoin = new BackgroundImage(imageJoin,
				BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
				new BackgroundSize(start_m.getWidth(), start_m.getHeight(), true, true, true, false));
		var newBJoin = new Background(bImageJoin);
		start_m.setBackground(newBJoin);
		//OnActionListener is in Windows.java -> addMenu()
		//endregion

		//region Exit button
		exit_m = new Button();
		exit_m.setMinHeight(90);
		exit_m.setMinWidth(140);
		var inputExit = new FileInputStream("res/exitgamebuttongimp.png");
		var imageExit = new Image(inputExit);
		var bImageExit = new BackgroundImage(imageExit,
				BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
				new BackgroundSize(exit_m.getWidth(), exit_m.getHeight(), true, true, true, false));
		var newBExit = new Background(bImageExit);
		exit_m.setBackground(newBExit);
		exit_m.setOnAction(actionEvent -> Platform.exit());
		//endregion

		main_menu = new VBox(30);
		main_menu.getChildren().addAll(start_m, settings_m, exit_m);
	}

	public Button startGameButton () { return start_m; }

	public void volumeControl () { player.volumeProperty().bind(volume.valueProperty().divide(100)); }

	public static void volumeControl (MediaPlayer player, Slider volume) { player.volumeProperty().bind(volume.valueProperty().divide(100)); }

	public VBox getBar () {
		if (main_menu == null) System.err.println("main_menu is null");

		return main_menu;
	}

	public VBox getMenuVBox () {
		try {
			//region Audio Settings
			settings_m = new MenuButton("", new ImageView(), audioItem_on, audioItem_off, audio);
			settings_m.setMinHeight(90);
			settings_m.setMinWidth(140);
			var inputAudio = new FileInputStream("res/audiogamebuttongimp.png");
			var imageAudio = new Image(inputAudio);
			var bImageAudio = new BackgroundImage(imageAudio,
					BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
					new BackgroundSize(settings_m.getWidth(), settings_m.getHeight(), true, true, true, false));
			var newBAudio = new Background(bImageAudio);
			settings_m.setBackground(newBAudio);

			File audio_path = new File("res/MenuMusic.mp3");
			volume = new Slider();
			Media media = new Media(audio_path.toURI().toString());
			player        = new MediaPlayer(media);
			audioItem_on  = new MenuItem("Audio On");
			audioItem_off = new MenuItem("Audio Off");
			audio         = new CustomMenuItem(volume);
			audio.setHideOnClick(false);

			audioItem_on.setOnAction(actionEvent -> player.setMute(false));
			audioItem_off.setOnAction(actionEvent -> player.setMute(true));
			audio.setOnAction(actionEvent -> volumeControl(player, volume));

			player.setAutoPlay(false);
			player.setOnEndOfMedia(() -> {
				player.seek(Duration.ZERO);
				player.play();
			});
			player.setOnError(() -> System.err.println("Error in playing bgm"));
			//endregion

			//region Play button
			start_m = new Button();
			start_m.setMinHeight(90);
			start_m.setMinWidth(140);
			var inputJoin = new FileInputStream("res/joingamebuttongimp.png");
			var imageJoin = new Image(inputJoin);
			var bImageJoin = new BackgroundImage(imageJoin,
					BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
					new BackgroundSize(start_m.getWidth(), start_m.getHeight(), true, true, true, false));
			var newBJoin = new Background(bImageJoin);
			start_m.setBackground(newBJoin);
			start_m.setOnAction((event) -> {
				player.stop();
				Platform.runLater(() -> new ClientController().start(SceneManager.getInstance().getMainWindow()));
			});
			//endregion



			//region Exit button
			exit_m = new Button();
			exit_m.setMinHeight(90);
			exit_m.setMinWidth(140);
			var inputExit = new FileInputStream("res/exitgamebuttongimp.png");
			var imageExit = new Image(inputExit);
			var bImageExit = new BackgroundImage(imageExit,
					BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
					new BackgroundSize(exit_m.getWidth(), exit_m.getHeight(), true, true, true, false));
			var newBExit = new Background(bImageExit);
			exit_m.setBackground(newBExit);
			exit_m.setOnAction(actionEvent ->  {
				Platform.exit();
				System.exit(0);
			});
			//endregion

			main_menu = new VBox(30);
			main_menu.getChildren().addAll(start_m, settings_m, exit_m);
		} catch (Exception e) {
			e.printStackTrace();
		}

        return main_menu;
	}
}