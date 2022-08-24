package com.halflife3.Controller;

import com.halflife3.Controller.Exceptions.SceneDoesNotExistException;
import com.halflife3.Controller.Exceptions.SceneStackEmptyException;
import com.halflife3.Controller.Exceptions.StageStackEmptyException;
import com.halflife3.GameUI.WindowAttributes;
import com.halflife3.Networking.NetworkingUtilities;
import javafx.application.*;
import javafx.scene.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.stage.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Objects;
import java.util.Stack;

/**
 * @author Johnathon Kemp - HalfLife Team Project
 * @version 1.0.0
 * <p>
 * This class is a singleton which deals with the scenes, and changing between them. When a scene is added, it is first put into the {@code
 * Scenes} hash map, along with a string to refer to that scene with in the future. This means that future scenes are cached, and therefore
 * do not need to be loaded every time a scene is needed.
 * <p>
 * As and when you want to revert to a previous scene, the {@code sceneOrder} stack keeps a track of all scenes set on the main window,
 * meaning that when {@code restorePreviousScene} is called, the first value is {@code pop}'ed off and not captured as it isn't needed, and
 * then {@code setScene} is called with the {@code peek()}'d value of the stack.
 * <p>
 * As this is a singleton pattern, the {@code instance} variable is used to store a static reference to this object, meaning this entire
 * class can be accessed as if it was all static (but also reduces the amount of space taken up in permanent generation [the area in the JVM
 * where static variables are stored]).
 */
public final class SceneManager {
	private Stage                  mainWindow;
	private HashMap<String, Stage> Stages     = new HashMap<>();
	private HashMap<String, Scene> Scenes     = new HashMap<>();
	private Stack<String>          sceneOrder = new Stack<>();
	private Stack<String>          stageOrder = new Stack<>();
	private WindowAttributes       windowAttributes;

	private static SceneManager instance;

	private SceneManager() {
		LoadWindowAttributes();
	} //Singleton, don't want to be able to create instances of this class

	//region Get Set Main Window
	/**
	 * A method to get the main stage from this SceneManager reference
	 *
	 * @return The Stage reference held by {@code mainWindow}
	 */
	public Stage getMainWindow() { return mainWindow; }

	/**
	 * A method to set the main stage of this SceneManager reference (should only really be used once)
	 *
	 * @param stage The stage to set the {@code mainWindow} variable to
	 */
	public void setMainWindow(String label, Stage stage) {
		setMainStageAttributes(stage);
		Stages.put(label, stage);
		stageOrder.push(label);
		this.mainWindow = stage;
	}

	//endregion
	/** A method to show the main stage contained in this SceneManager reference */
	public void showWindow() { mainWindow.show(); }

	/** A method to hide the main stage contained in this SceneManager reference */
	public void hideWindow() { mainWindow.hide(); }
	//region SetScene
	/**
	 * A method to set the {@code mainWindow} scene, as well as add it to the {@code Scenes} hashmap
	 *
	 * @param label The label to associate with the scene
	 * @param scene The scene to set
	 */
	public void setScene(String label, Scene scene) {
		addScene(label, scene);
		setSceneCursor(scene);
		mainWindow.setScene(scene);
		sceneOrder.push(label);
		mainWindow.setTitle(label);
	}

	/**
	 * A method to set the cursor of the scene. For simplicity's sake, this cursor is the one used in the game modes, and now in every
	 * scene so we don't have to worry about the cursor disappearing between scenes.
	 *
	 * @param scene The scene to set the cursor of
	 */
	private void setSceneCursor(Scene scene) throws NullPointerException {
		try (var resStream = getClass().getClassLoader().getResourceAsStream("Sprites/Cursor/crosshair.png")) {
			assert resStream != null;

			Image image = new Image(resStream);
			scene.setCursor(new ImageCursor(image, image.getWidth() / 2, image.getHeight() / 2));
		} catch (Exception e) {
			throw new NullPointerException("SceneManager cannot set the cursor!");
		}
	}

	/**
	 * A method to set the {@code mainWindow} scene with a currently cached scene
	 *
	 * @param label The label of the scene to set
	 *
	 * @throws SceneDoesNotExistException If the scene doesn't exist in the Scenes stack
	 */
	public void setScene(String label) throws SceneDoesNotExistException {
		mainWindow.setScene(getScene(label));
		sceneOrder.push(label);
		mainWindow.setTitle(label);
	}
	//endregion

	/** A method to restore the previous scene as the currently set scene in SceneManager */
	public void restorePreviousScene() throws SceneStackEmptyException {
		if (sceneOrder.size() == 1)
			throw new SceneStackEmptyException("The scene stack only contains one value! No scene to restore");
		sceneOrder.pop();
		mainWindow.setScene(Scenes.get(sceneOrder.peek()));
		mainWindow.setTitle(sceneOrder.peek());
	}

	/** A method to restore the previous stage as the currently set main stage in SceneManager */
	public void restorePreviousStage() throws StageStackEmptyException {
		if (stageOrder.size() == 1)
			throw new StageStackEmptyException("The stage stack only contains one value! No stage to restore");
	}

	/**
	 * You... Monster... You euthanized your faithful SceneManager data more quickly than any test subject on record.
	 * Congratulations
	 * <p>
	 * (This method completely removes all data from the object, clearing it out in the hopes that garbage collection
	 * comes along and cleans it up)
	 */
	public void euthanizeData() {
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
	 *
	 * @return The Scene with the given label
	 *
	 * @throws SceneDoesNotExistException If the given label does not exist in the {@code Scenes} hash map
	 */
	private Scene getScene(String label) throws SceneDoesNotExistException {
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
	private void addScene(String label, Scene scene) {
		if (!Scenes.containsKey(label))
			Scenes.put(label, scene);
	}

	/**
	 * Set the properties of the stage (called every time a new stage is set, should only occur once however)
	 *
	 * @param s The stage to set the property of
	 */
	private void setMainStageAttributes(Stage s) {
		s.setTitle(windowAttributes.title);
		s.setResizable(windowAttributes.resizeable);
		s.setMaximized(windowAttributes.maximisedOnLoad);
		s.setFullScreen(windowAttributes.fullScreenOnLoad);

		if (!windowAttributes.decorated)
			s.initStyle(StageStyle.UNIFIED);
		if (windowAttributes.isModal)
			s.initModality(Modality.APPLICATION_MODAL);
		if (windowAttributes.maximisedOnLoad)
			s.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);

		try {
			s.getIcons().add(new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("Sprites/Ball/Ball.png"))));
		} catch (Exception ignored) {}
	}

	/** A method to load the saved window attributes from disk. This is used to preserve user set variables */
	private void LoadWindowAttributes() {
		try (var fi = new FileInputStream(new File("AppData/config.conf"))) {
			try (var oi = new ObjectInputStream(fi)) {
				windowAttributes = (WindowAttributes) oi.readObject();
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			if (Files.notExists(Paths.get("AppData"))) {
				boolean createdAppData = new File("AppData").mkdir();
				if (!createdAppData) {
					NetworkingUtilities.CreateErrorMessage("Could not create AppData directory",
							"Error Creating AppData Directory",
							"AppData file could not be created. Please check that files can be created in the root directory!");

					//Error shown, now end the application
					Platform.exit();
					System.exit(-1);
				}
			}
			try (var fo = new FileOutputStream(new File("AppData/config.conf"))) {
				try (var os = new ObjectOutputStream(fo)) {
					WindowAttributes genericAttributes = new WindowAttributes();
					genericAttributes.title            = "Main Menu";
					genericAttributes.resizeable       = false;
					genericAttributes.maximisedOnLoad  = false;  //Change to false -> Main Menu not fullscreen
					genericAttributes.fullScreenOnLoad = false; //Change to false -> Main Menu not fullscreen
					genericAttributes.isModal          = false;
					genericAttributes.decorated        = true;

					os.writeObject(genericAttributes);
					windowAttributes = genericAttributes;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	//endregion

	/**
	 * A method to return the static reference to this class
	 *
	 * @return The static reference to this class
	 */
	public static SceneManager getInstance() {
		if (instance == null)
			instance = new SceneManager();
		return instance;
	}
}