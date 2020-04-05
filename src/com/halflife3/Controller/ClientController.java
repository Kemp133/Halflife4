package com.halflife3.Controller;

import com.halflife3.Controller.Exceptions.SceneStackEmptyException;
import com.halflife3.Controller.GameModes.GameMode;
import com.halflife3.Controller.GameModes.MainMode;
import com.halflife3.GameUI.MainMenu;
import com.halflife3.Networking.NetworkingUtilities;
import javafx.animation.AnimationTimer;
import javafx.stage.Stage;

/**
 * This class is used to deal with the client side control flow of the game. This class while only dealing with the
 * MainMode game mode could be extended to play any amount of game modes. This class itself has to deal with the fact
 * that JavaFX is incredibly funky, and the animation timer has to be called inside of start otherwise it likes to
 * complain (hence why the run logic is stored here, which isn't too much of an inconvenience anyways).
 *
 * Wherever the game sets the scene in {@code SceneManager} should also set it back in the same place to try and avoid
 * errors with incorrectly using the SceneManager.
 *
 * This class also keeps track of the gamemode in {@code gamemode} as well as the FPS target that the game mode should be
 * played at. Hopefully in the (near) future I can move the FPS into the game mode and have this value encapsulated
 * fully.
 */
public class ClientController extends BaseController {
	private       GameMode gamemode;
	private final int      FPS = 30;

	@Override
	public void initialise() {
		gamemode = new MainMode("Ball Thing", 1);
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

				//region Calculate time since last update.
				elapsedTime = (currentNanoTime - lastUpdate) / 1e9;
				lastUpdate  = currentNanoTime;
				//endregion

				gamemode.gameLoop(elapsedTime);

				if (gamemode.hasFinished) {
					this.stop();
					gamemode = null;
					try {
						SceneManager.getInstance().restorePreviousScene();
					} catch (SceneStackEmptyException e) {
						NetworkingUtilities.CreateErrorMessage("No Scenes To Revert To", "The scene stack only contains one element!", e.getMessage());
					}
				}
			}
		}.start();

		((MainMode) gamemode).root.requestFocus();
		MainMenu main = new MainMenu();
		SceneManager.getInstance().setScene("Main Menu", main.getScene());
	}

	@Override
	public void stop() {
		end();
	}

	@Override
	public void end() {
		//gamemode = null;
		//SceneManager.getInstance().restorePreviousScene(); //Can't call this inside of animation timer
	}

	@Override
	public void run() {
		initialise();
		start();
	}
}
