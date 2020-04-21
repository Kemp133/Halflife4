package com.halflife3.Controller;

import com.halflife3.GameModes.*;
import com.halflife3.GameUI.*;
import javafx.animation.*;
import javafx.stage.*;

/**
 * This class is used to deal with the client side control flow of the game. This class while only dealing with the
 * MainMode game mode could be extended to play any amount of game modes. This class itself has to deal with the fact
 * that JavaFX is incredibly funky, and the animation timer has to be called inside of start otherwise it likes to
 * complain (hence why the run logic is stored here, which isn't too much of an inconvenience anyways).
 * <p>
 * Wherever the game sets the scene in {@code SceneManager} should also set it back in the same place to try and avoid
 * errors with incorrectly using the SceneManager.
 * <p>
 * This class also keeps track of the game mode in {@code gamemode} as well as the FPS target that the game mode
 * should be played at.
 */
public class ClientController extends BaseController {
	/** A private reference to the game mode that is currently running */
	private             GameMode gamemode;
	/** A static final reference to the target FPS that the game should try and play at */
	public static final int      FPS = 30;

	@Override
	public void initialise() {
		gamemode = new MainMode("Ball Thing", 3);
	}

	@Override
	public void start(Stage stage) {
		run();
	}

	@Override
	public void start() {
		gamemode.initialise();

		new AnimationTimer() {
			long lastUpdate = System.nanoTime();
			double elapsedTime = 0;

			public void handle(long currentNanoTime) {
				if (currentNanoTime - lastUpdate < 1e9 / (FPS * 1.07))
					return;

				elapsedTime = (currentNanoTime - lastUpdate) / 1e9; // Calculate time since last update.
				lastUpdate  = currentNanoTime;

				gamemode.gameLoop(elapsedTime);

				if (gamemode.hasFinished) {
					this.stop();
					ClientController.this.stop();
//					MainMenu main = new MainMenu();
//					SceneManager.getInstance().setScene("Main Menu", main.getScene());
				}
			}
		}.start();
		((MainMode) gamemode).root.requestFocus();
	}

	@Override
	public void stop() {
		end();
	}

	@Override
	public void end() {
		if (gamemode.win) {
			WinScene win = new WinScene();
			SceneManager.getInstance().setScene("Win Scene", win.getScene());
		} else {
			LoseScene lose = new LoseScene();
			SceneManager.getInstance().setScene("Lose Scene", lose.getScene());
		}
		gamemode = null;
		ObjectManager.resetObjects();
	}

	@Override
	public void run() {
		initialise();
		start();
	}
}
