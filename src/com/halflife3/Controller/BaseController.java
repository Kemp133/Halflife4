package com.halflife3.Controller;

import com.halflife3.Controller.Interfaces.IController;
import com.halflife3.GameUI.ApplicationUser;
import javafx.application.*;

/**
 * This class acts as the basis for all controllers made in this game. This class contains common fields that all controllers require, as
 * well as containing useful methods which help manipulate the controllers in useful ways.
 * <p>
 * These controllers should aim to mimic the controllers found in a MVC project (i.e. Model, View, Controller), where all the implementation
 * detail and logic handling is done in the class, leaving the views to create the visual layouts and the models to act as data containers
 * between the views and controllers.
 * <p>
 * This class also provides a static {@code ApplicationUser} reference, which means that the log in details of a user can be gotten from
 * anywhere in the project and will get the username of the logged in player at runtime. As well as this, it contains a {@code
 * closeApplication()} method which contains code to automatically clean up the resources the class used before calling {@code
 * Platform.exit()} (synonymous to System.exit(), but for JavaFX), and then {@code System.exit(0)} straight afterwards. This is done to free
 * the resources the program used, with the exit() methods being done such that {@code Platform.exit()} closes the JavaFX main thread, and
 * {@code System.exit()} stops the Java application altogether.
 */
public abstract class BaseController extends Application implements IController {
	/**
	 * A reference to {@code SceneManager}, which can be used to store a reference to {@code SceneManager.getInstance()} for brevity's sake
	 */
	protected        SceneManager    manager;
	/**
	 * A static reference to an {@code ApplicationUser}, used so that the username of the player can be accessed from anywhere as this
	 * field is guaranteed to be populated
	 */
	protected static ApplicationUser user;

	/**
	 * A method used to return the static reference to {@code ApplicationUser} which this class holds
	 *
	 * @return The {@code ApplicationUser} details of the current user, which contains a username of the current player
	 */
	public static ApplicationUser GetApplicationUser() { return user; }

	/** A method which frees up all the resources this class was using, and then closes the program down correctly */
	public void closeApplication() {
		if(manager != null)
			manager.euthanizeData();
		user = null;
		Platform.exit();
		System.exit(0);
	}
}