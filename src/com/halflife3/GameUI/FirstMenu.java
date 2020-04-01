package com.halflife3.GameUI;

import com.halflife3.DatabaseUI.Login;
import com.halflife3.GameUI.interfaces.ICredentialUser;
import com.halflife3.Networking.Server.Server;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

//The menu set for choose server and client
public class FirstMenu extends Application implements ICredentialUser {
	public static final double SCREEN_WIDTH  = 800;
	public static final double SCREEN_HEIGHT = 600;

	//region Variables
	private        Stage            pStage = null;
	private        StackPane        root   = new StackPane();
	private static       Pane             back;
	private static       Button           client_m;
	private static       Button           server_m;
	private static ApplicationUser  user;
	private static WindowAttributes windowAttributes;
	//endregion

	public FirstMenu () throws FileNotFoundException {
		var input1 = new FileInputStream("res/Client.png");
		var input2 = new FileInputStream("res/create_server.png");
		var input3 = new FileInputStream("res/MenuBackground.png");

		var image1 = new Image(input1);
		var image2 = new Image(input2);
		var image3 = new Image(input3);

		back = new Pane(new ImageView(image3));

		client_m = new Button("", new ImageView(image1));
		server_m = new Button("", new ImageView(image2));

		server_m.setOnAction(e -> {
			try {
				runServer();
			} catch (Exception ex) { ex.printStackTrace(); }
		});

		client_m.setOnAction(e -> {
			try {
				new Windows().start(pStage);
			} catch (FileNotFoundException ex) { ex.printStackTrace(); }
		});
	}

	static {
		try {
			var input1 = new FileInputStream("res/Client.png");
			var input2 = new FileInputStream("res/create_server.png");
			var input3 = new FileInputStream("res/MenuBackground.png");

			var image1 = new Image(input1);
			var image2 = new Image(input2);
			var image3 = new Image(input3);

			back = new Pane(new ImageView(image3));

			client_m = new Button("", new ImageView(image1));
			server_m = new Button("", new ImageView(image2));
		} catch(Exception e) {
			e.printStackTrace();
		}

		server_m.setOnAction(e -> {
//			try {
////				runServer();
//			} catch (Exception ex) { ex.printStackTrace(); }
		});

		client_m.setOnAction(e -> {
//			try {
////				new Windows().start(pStage);
//			} catch (FileNotFoundException ex) { ex.printStackTrace(); }
		});
	}

	private void runServer () {
		pStage.hide();
		Server server = new Server();
		server.start();
	}

	public static void main (String[] args) {
		System.setProperty("javafx.preloader", Login.class.getName());
		LoadWindowAttributes();
		Application.launch(args); //Calls: FirstMenu() -> start()
	}

	private static void LoadWindowAttributes () {
		try (var fi = new FileInputStream(new File("AppData/config.conf"))) {
			try (var oi = new ObjectInputStream(fi)) {
				windowAttributes = (WindowAttributes) oi.readObject();
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			if (Files.notExists(Paths.get("AppData"))) {
				boolean createdAppData = new File("AppData").mkdir();
				if (!createdAppData) {
					Alert alert = new Alert(Alert.AlertType.ERROR);
					alert.setTitle("Could not create AppData directory");
					alert.setHeaderText("Error Creating AppData Directory");
					alert.setContentText("AppData file could not be created. Please check that files can be created in the root directory!");
					alert.show();

					//Error shown, now end the application
					Platform.exit();
					System.exit(-1);
				}
			}
			try (var fo = new FileOutputStream(new File("AppData/config.conf"))) {
				try (var os = new ObjectOutputStream(fo)) {
					WindowAttributes genericAttributes = new WindowAttributes();
					genericAttributes.title            = "Main Menu";
					genericAttributes.resizeable       = false;
					genericAttributes.maximisedOnLoad  = false;  //Change to false -> Main Menu not fullscreen
					genericAttributes.fullScreenOnLoad = false; //Change to false -> Main Menu not fullscreen
					genericAttributes.isModal          = false;
					genericAttributes.decorated        = false;
//                    genericAttributes.iconPath = "res/halflife3.bmp";

					os.writeObject(genericAttributes);
					windowAttributes = genericAttributes;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void start (Stage stage) {
		setPStage(stage);
		setMainStage(stage);
		Scene scene = new Scene(createContent(), SCREEN_WIDTH, SCREEN_HEIGHT, Color.WHITE);
		stage.setScene(scene);
		mayBeShown();
	}

	public static Scene getStartMenuScene() {
		return new Scene(createContent(new StackPane()), SCREEN_WIDTH, SCREEN_HEIGHT, Color.WHITE);
	}

	private void setPStage (Stage pStage) { this.pStage = pStage; }

	private void setMainStage (Stage s) {
		s.setTitle(windowAttributes.title);
		s.setResizable(windowAttributes.resizeable);
		s.setMaximized(windowAttributes.maximisedOnLoad);
		s.setFullScreen(windowAttributes.fullScreenOnLoad);

		if (!windowAttributes.decorated) s.initStyle(StageStyle.UNIFIED);
		if (windowAttributes.isModal) s.initModality(Modality.APPLICATION_MODAL);
		if (windowAttributes.maximisedOnLoad) s.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);

//        if(!windowAttributes.iconPath.isEmpty() || !windowAttributes.iconPath.isBlank()) {
//            try (FileInputStream fi = new FileInputStream(windowAttributes.iconPath)) {
//                s.getIcons().add(new Image(fi));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
	}

	private StackPane createContent () {
		VBox pane = new VBox(20);
		pane.getChildren().add(back);
		pane.getChildren().addAll(server_m, client_m);
		pane.setAlignment(Pos.CENTER);
		root.getChildren().add(pane);
		return root;
	}

	private static StackPane createContent(StackPane root) {
		VBox pane = new VBox(20);
		pane.getChildren().add(back);
		pane.getChildren().addAll(server_m, client_m);
		pane.setAlignment(Pos.CENTER);
		root.getChildren().add(pane);
		return root;
	}

	private Pane getBack () { return back; }

	@Override
	public void setApplicationUser (String username) {
		user                = new ApplicationUser();
		user.username       = username;
		user.isValidSession = true;
		mayBeShown();
	}

	private void mayBeShown () {
		if (user != null && pStage != null)
			Platform.runLater(() -> pStage.show());
	}
}
