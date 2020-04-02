package com.halflife3.Controller;

import com.halflife3.Controller.Interfaces.IController;
import com.halflife3.DatabaseUI.Login;
import com.halflife3.GameUI.ApplicationUser;
import com.halflife3.GameUI.Windows;
import com.halflife3.GameUI.interfaces.ICredentialUser;
import javafx.application.Application;
import javafx.stage.Stage;

public class BaseController extends Application implements ICredentialUser, IController {
	private SceneManager    manager;
	private ApplicationUser user;

	public static void main (String[] args) {
		System.setProperty("javafx.preloader", Login.class.getName());
		Application.launch(args);
	}

	@Override
	public void start (Stage stage) {
		manager = SceneManager.getInstance();
		manager.setMainWindow(stage);
		run();
	}

	@Override
	public void initialise () {} //Never called

	@Override
	public void start () {
		try {
			manager.setScene("Main Menu", Windows.getMenuScene());
		} catch (Exception e) {
			e.printStackTrace();
		}
		manager.showWindow();
	}

	@Override
	public void end () { } //Not needed for this controller

	@Override
	public void run () {
		start();
	}

	@Override
	public void setApplicationUser (String username) { this.user = new ApplicationUser(username, true); }
}
