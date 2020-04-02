package com.halflife3.Controller;

import com.halflife3.Controller.GameModes.GameMode;
import com.halflife3.Controller.GameModes.MainMode;
import com.halflife3.Controller.Interfaces.IController;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.stage.Stage;

public class ClientController extends Application implements IController {
	private       GameMode gamemode;
	private final int      FPS = 30;

	@Override
	public void initialise () {
		gamemode = new MainMode("Ball Thing", 3);
	}

	@Override
	public void start (Stage stage) {
		run();
	}

	@Override
	public void start () {
		gamemode.initialise();

		new AnimationTimer() {
			long lastUpdate = System.nanoTime();
			double elapsedTime = 0;

			public void handle (long currentNanoTime) {
				if (currentNanoTime - lastUpdate < 1e9 / (FPS * 1.07))
					return;

				//region Calculate time since last update.
				elapsedTime = (currentNanoTime - lastUpdate) / 1e9;
				lastUpdate  = currentNanoTime;
				//endregion

				gamemode.gameLoop(elapsedTime);

				if(gamemode.hasFinished) {
					this.stop();
					gamemode = null;
					SceneManager.getInstance().restorePreviousScene();
				}
			}
		}.start();

		((MainMode)gamemode).root.requestFocus();
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
	}
}
