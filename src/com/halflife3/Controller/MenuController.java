package com.halflife3.Controller;

import com.halflife3.DatabaseUI.Login;
import com.halflife3.GameUI.ApplicationUser;
import com.halflife3.GameUI.MainMenu;
import com.halflife3.GameUI.interfaces.ICredentialUser;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class MenuController extends BaseController implements ICredentialUser {
	private MainMenu menu;

	public static void main(String[] args) {
		System.setProperty("javafx.preloader", Login.class.getName());
		Application.launch(args);
	}

	@Override
	public void start(Stage stage) {
		manager = SceneManager.getInstance();
		manager.setMainWindow("Main Menu", stage);
		run();
		mayBeShown();
	}

	@Override
	public void start() {
		menu = new MainMenu();
		menu.getPlayer().play();
		menu.getExit().setOnAction(actionEvent -> {
			end();
		});
		SceneManager.getInstance().setScene("Main Menu", menu.getScene());
	}

	@Override
	public void initialise() {} //Can't think of a reason to call this

	@Override
	public void end() {
		manager.euthanizeData();
		user = null;
		menu = null;
		Platform.exit();
		System.exit(0);
	}

	@Override
	public void run() {
		start();
	}

	@Override
	public void setApplicationUser(String username) {
		user = new ApplicationUser(username, true);
		mayBeShown();
	}

	private void mayBeShown() {
		if (user != null && SceneManager.getInstance().getMainWindow() != null)
			Platform.runLater(() -> SceneManager.getInstance().showWindow());
	}
}