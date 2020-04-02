package com.halflife3.Controller;

import com.halflife3.Controller.GameModes.GameMode;
import com.halflife3.Controller.GameModes.MainMode;
import com.halflife3.Controller.Interfaces.IController;
import javafx.application.Application;
import javafx.stage.Stage;

public class ClientController extends Application implements IController {
	private GameMode     gamemode;

	@Override
	public void initialise () {
		gamemode = new MainMode("Ball Thing", 5);
	}

	@Override
	public void start (Stage stage) {
		run();
	}

	@Override
	public void start () {
		gamemode.runGame();
	}

	@Override
	public void stop () {
		end();
	}

	@Override
	public void end () {
		gamemode = null;
		SceneManager.getInstance().restorePreviousScene(); //Exit this scene, go back to the menu
	}

	@Override
	public void run () {
		initialise();
		start();
		end();
	}
}
