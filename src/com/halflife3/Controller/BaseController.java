package com.halflife3.Controller;

import com.halflife3.Controller.Interfaces.IController;
import com.halflife3.DatabaseUI.Login;
import com.halflife3.GameUI.ApplicationUser;
import com.halflife3.GameUI.MainMenu;
import com.halflife3.GameUI.WindowAttributes;
import com.halflife3.GameUI.Windows;
import com.halflife3.GameUI.interfaces.ICredentialUser;
import com.halflife3.Networking.NetworkingUtilities;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.input.KeyCombination;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class BaseController extends Application implements ICredentialUser, IController {
	private        SceneManager     manager;
	private        ApplicationUser  user;
	private static WindowAttributes windowAttributes;
	private        MainMenu         menu;

	public static void main (String[] args) {
		System.setProperty("javafx.preloader", Login.class.getName());
		LoadWindowAttributes();
		Application.launch(args);
	}

	@Override
	public void start (Stage stage) {
		manager = SceneManager.getInstance();
		setMainStage(stage);
		manager.setMainWindow(stage);
		run();
		mayBeShown();
	}

	private void setMainStage (Stage s) {
		s.setTitle(windowAttributes.title);
		s.setResizable(windowAttributes.resizeable);
		s.setMaximized(windowAttributes.maximisedOnLoad);
		s.setFullScreen(windowAttributes.fullScreenOnLoad);

		if (!windowAttributes.decorated) s.initStyle(StageStyle.UNIFIED);
		if (windowAttributes.isModal) s.initModality(Modality.APPLICATION_MODAL);
		if (windowAttributes.maximisedOnLoad) s.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
	}

	@Override
	public void start () {
		menu = new MainMenu();
		menu.getPlayer().play();
		manager.setScene("Main Menu", menu.getScene());
	}

	@Override
	public void initialise () {} //Never called

	@Override
	public void end () {
		manager.eutanizeData();
		user = null;
		windowAttributes = null;
		menu = null;
	}

	@Override
	public void run () {
		start();
	}

	@Override
	public void setApplicationUser (String username) {
		user = new ApplicationUser(username, true);
		mayBeShown();
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
					NetworkingUtilities.CreateErrorMessage(
							"Could not create AppData directory",
							"Error Creating AppData Directory",
							"AppData file could not be created. Please check that files can be created in the root directory!"
					);

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

	private void mayBeShown () {
		if (user != null && SceneManager.getInstance().getMainWindow() != null)
			Platform.runLater(() -> SceneManager.getInstance().showWindow());
	}
}