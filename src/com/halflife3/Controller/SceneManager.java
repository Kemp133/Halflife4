package com.halflife3.Controller;

import com.halflife3.Controller.Exceptions.SceneDoesNotExistException;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Stack;

/**
 * @author Johnathon Kemp - HalfLife Team Project
 * @version 1.0.0
 *
 * This class is a singleton which deals with the scenes, and changing between them. When a scene is added, it is first
 * put into the {@code Scenes} hash map, along with a string to refer to that scene with in the future. This means that
 * future scenes are cached, and therefore do not need to be loaded every time a scene is needed. As and when you want
 * to revert to a previous scene, the {@code sceneOrder} stack keeps a track of all scenes set on the main window,
 * meaning that when {@code restorePreviousScene} is called, the first value is {@code pop}'ed off and not captured as
 * it isn't needed, and then {@code setScene} is called with the {@code peek()}'d value of the stack. As this is a
 * singleton pattern, the {@code instance} variable is used to store a static reference to this object, meaning one
 * reference can be accessed from anywhere.
 * */
public final class SceneManager {
	private Stage                  mainWindow;
	private HashMap<String, Scene> Scenes = new HashMap<>();
	private static SceneManager    instance;
	private static Stack<String>   sceneOrder = new Stack<>();

	private SceneManager () {} //Singleton, don't want to be able to create instances

	//region Get/Set Main Window
	public Stage getMainWindow() { return mainWindow; }
	public void setMainWindow (Stage mainWindow) {
		this.mainWindow = mainWindow;
	}
	//endregion

	public void showWindow() { mainWindow.show(); }

	//region SetScene
	/**
	 * A method to set the {@code mainWindow} scene, as well as add it to the {@code Scenes} hashmap
	 *
	 * @param label The label to associate with the scene
	 * @param scene The scene to set
	 */
	public void setScene (String label, Scene scene) {
		addScene(label, scene);
		mainWindow.setScene(scene);
		sceneOrder.push(label);
	}

	/**
	 * A method to set the {@code mainWindow} scene with a currently cached scene
	 *
	 * @param label The label of the scene to set
	 * @throws SceneDoesNotExistException If the scene doesn't exist in the
	 */
	public void setScene (String label) throws SceneDoesNotExistException {
		mainWindow.setScene(getScene(label));
		sceneOrder.push(label);
	}
	//endregion

	/** A method to restore the previous scene as the currently set scene on the SceneManager  */
	public void restorePreviousScene() {
		sceneOrder.pop();
		mainWindow.setScene(Scenes.get(sceneOrder.peek()));
	}

	/** You... Monster... You euthanized your faithful SceneManager data more quickly than any test
	 * subject on record. Congratulations
	 *
	 * (This method completely removes all data from the object, clearing it out in the hopes that garbage
	 * collection comes along and cleans it up)
	 * */
	public void euthanizeData () {
		mainWindow = null;
		Scenes.clear();
		instance = null;
		sceneOrder.clear();
	}

	//region HelperMethods
	/**
	 * A helper method to get a scene out of the {@code Scenes} hash map with the label {@code label}
	 *
	 * @param label The label of the scene to load
	 * @return The Scene with the given label
	 * @throws SceneDoesNotExistException If the given label does not exist in the {@code Scenes} hash map
	 */
	private Scene getScene (String label) throws SceneDoesNotExistException {
		if (!Scenes.containsKey(label))
			throw new SceneDoesNotExistException("The given label does not correspond to a stored scene!");
		return Scenes.get(label);
	}
	/**
	 * A helper method to add a scene with a given label to the {@code Scenes} hash map
	 *
	 * @param label The label to associate with the given scene
	 * @param scene The scene to add to the hash map
	 */
	private void addScene (String label, Scene scene) {
		if (!Scenes.containsKey(label))
			Scenes.put(label, scene);
	}

	public static SceneManager getInstance() {
		if(instance == null)
			instance = new SceneManager();
		return instance;
	}
	//endregion
}