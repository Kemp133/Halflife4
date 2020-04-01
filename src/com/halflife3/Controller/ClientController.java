package com.halflife3.Controller;

import com.halflife3.Controller.GameModes.GameMode;
import com.halflife3.Controller.GameModes.MainMode;
import com.halflife3.Controller.Interfaces.IController;
import javafx.application.Application;
import javafx.stage.Stage;

public class ClientController extends Application implements IController {
	private SceneManager manager;
	private GameMode     gamemode;

	@Override
	public void start (Stage stage) throws Exception {

	}

	@Override
	public void initialise (SceneManager manager) {
		this.manager = manager;
	}

	@Override
	public void start () {
		gamemode = new MainMode("Ball Thing", 5);
	}

	@Override
	public void end () {

	}

	@Override
	public void run (SceneManager manager) {
		initialise(manager);
		start();
	}
}
