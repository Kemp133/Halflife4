package com.halflife3.Controller;

import com.halflife3.Controller.Exceptions.SceneDoesNotExistException;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.HashMap;

public final class SceneManager {
	private Stage                  mainWindow;
	private HashMap<String, Scene> Scenes = new HashMap<>();

	public SceneManager () {}

	public void setMainWindow (Stage mainWindow) {
		this.mainWindow = mainWindow;
	}

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
	}

	/**
	 * A method to set the {@code mainWindow} scene with a currently cached scene
	 *
	 * @param label The label of the scene to set
	 * @throws SceneDoesNotExistException If the scene doesn't exist in the
	 */
	public void setScene (String label) throws SceneDoesNotExistException {
		mainWindow.setScene(getScene(label));
	}
	//endregion

	//region HelperMethods
	/**
	 * A helper method to get a scene out of the {@code Scenes} hashmap with the label {@code label}
	 *
	 * @param label The label of the scene to load
	 * @return The Scene with the given label
	 * @throws SceneDoesNotExistException If the given label does not exist in the {@code Scenes} hashmap
	 */
	private Scene getScene (String label) throws SceneDoesNotExistException {
		if (!Scenes.containsKey(label))
			throw new SceneDoesNotExistException("The given label does not correspond to a stored scene!");
		return Scenes.get(label);
	}
	/**
	 * A helper method to add a scene with a given label to the {@code Scenes} hashmap
	 *
	 * @param label The label to associate with the given scene
	 * @param scene The scene to add to the hashmap
	 */
	private void addScene (String label, Scene scene) {
		if (!Scenes.containsKey(label))
			Scenes.put(label, scene);
	}
	//endregion
}
