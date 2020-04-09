package com.halflife3.GameUI;

import com.halflife3.Controller.MapMenuController;
import com.halflife3.Controller.MenuController;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class Maps {
	static public String      Map  = "res/Maps/map1.png";
	private       Button      map0 = new Button("Map1");
	private       Button      map1 = new Button("Map2");
	private       Button      map2 = new Button("Map3");
	private       Button      back = new Button("Back to Main Menu");
	private       ImageView   imgView1;
	private       ImageView   imgView2;
	private       ImageView   imgView3;
	private ArrayList<String> mapList;

	private Scene scene;

	public Maps() {
		mapList = pathList();
		initialiseMenuScene();
	}

	private VBox vbox() {
		map0.setOnAction(actionEvent -> {
			Map = mapList.get(0);
			map0.getStyleClass().add("button1");
			map1.getStyleClass().remove("button1");
			map2.getStyleClass().remove("button1");
			var map_control = new MapMenuController();
			map_control.end();
		});

		map1.setOnAction(actionEvent -> {
			Map = mapList.get(1);
			map1.getStyleClass().add("button1");
			map0.getStyleClass().remove("button1");
			map2.getStyleClass().remove("button1");
			var map_control = new MapMenuController();
			map_control.end();
		});

		map2.setOnAction(actionEvent -> {
			Map = mapList.get(3);
			map2.getStyleClass().add("button1");
			map0.getStyleClass().remove("button1");
			map1.getStyleClass().remove("button1");
			var map_control = new MapMenuController();
			map_control.end();
		});

		back.setOnAction(actionEvent -> {
			var map_control = new MapMenuController();
			map_control.end();
		});

		getImage();

		HBox h1 = createHBox(map0, imgView1);
		HBox h2 = createHBox(map1, imgView2);
		HBox h3 = createHBox(map2, imgView3);

		VBox vbox = new VBox(h1, h2, h3, back);
		vbox.setAlignment(Pos.BASELINE_CENTER);
		vbox.setPadding(new Insets(30, 0, 0, 20));
		vbox.setSpacing(20);
		vbox.setStyle("-fx-background-color: rgba(176,224,230,0.8);");

		return vbox;
	}

	private HBox createHBox(Button map, ImageView imgView) {
		HBox h = new HBox(map, imgView);
		h.setAlignment(Pos.CENTER);
		h.setSpacing(40);
		return h;
	}

	private BorderPane borderPane() {
		BorderPane borderPane = new BorderPane();

		Font paladinFont = null;
		try {
			paladinFont = Font.loadFont(new FileInputStream(new File("res/Font/PaladinsSemiItalic.otf")), 40);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		Label  title  = new Label("Maps");
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
		BorderPane.setAlignment(title, Pos.TOP_CENTER);

		borderPane.setLeft(leftPane);
		borderPane.setRight(rightPane);
		borderPane.setBottom(bottomPane);

		borderPane.setBackground(MenuUtilitites.getBackground(getClass(), "res/Leaderboard/LeaderboardBackground" +
		                                                                  ".jpg"));
		borderPane.setCenter(vbox());

		File f = new File("res/MainMenu/MainMenuCSS.css");
		borderPane.getStylesheets().add("file:///" + f.getAbsolutePath().replace("\\", "/"));
		return borderPane;
	}

	private void initialiseMenuScene() {
		scene = new Scene(borderPane(), MenuController.GAME_WINDOW_WIDTH, MenuController.GAME_WINDOW_HEIGHT);
	}

	private void getImage() {
		try {
			Image mapImage1 = new Image(new FileInputStream(mapList.get(0)));
			Image mapImage2 = new Image(new FileInputStream(mapList.get(1)));
			Image mapImage3 = new Image(new FileInputStream(mapList.get(3)));
			imgView1 = new ImageView(mapImage1);
			imgView2 = new ImageView(mapImage2);
			imgView3 = new ImageView(mapImage3);
			imgView1.setFitHeight(100);
			imgView1.setFitWidth(220);
			imgView2.setFitHeight(100);
			imgView2.setFitWidth(220);
			imgView3.setFitHeight(100);
			imgView3.setFitWidth(220);
		} catch (IOException e) {
			System.err.println("Can't find Image.");
		}
	}

	public Scene getScene() { return scene; }

	private ArrayList<String> pathList() {
		ArrayList<String> paths = new ArrayList<>();

		File directory = new File("res\\Maps");
		int fileCount = Objects.requireNonNull(directory.list()).length;

		for (int i = 1; i <= fileCount; i++)
		     paths.add(String.format("res/Maps/map%d.png", i));

		return paths;
	}
}
