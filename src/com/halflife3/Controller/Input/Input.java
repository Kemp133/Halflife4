package com.halflife3.Controller.Input;

import com.halflife3.GameObjects.Vector2;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

import java.util.HashMap;

/**
 * @author Team Halflife
 * <p>
 * This class deals with the input for the program. It is a singleton pattern, meaning that it is accessible from anywhere, but all the
 * contents do not need to be static themselves. This class contains hash maps to act as a mapping between input keycodes, and what state
 * they're in. There are hash maps for three types of input, and this exists for both keyboard and mouse inputs. This class also stores the
 * mouse position in a {@code Vector2} object, keeping all input based logic neatly encapsulated in one class.
 * <p>
 * These consist of pressed, momentary (typed and clicked respectively), and released. Due to the pressed and released values being mutually
 * exclusive of each other, when a key is pressed (and hence the keyvalue in the pressed hash map is set to true), the corresponding entry
 * in the released hash map is also then set to false. This means that only the typed values need to be reset each loop, which reduces the
 * amount of time spent in the input class.
 * <p>
 * On creation (i.e the first time the getInstance method is called), the values in the hash map are initialised once and then are ready to
 * use. The getInstance method is also classed as enthusiastic initilisation as if the instance doesn't exist, it's cretated inside of this
 * method
 */
public class Input {
	/** A static reference to this class, which is required for the singleton pattern */
	public static Input instance;
	/**
	 * The constructor is private as we don't want people to be able to create an instance of this class on their own (a requirement of the
	 * singleton pattern)
	 */
	private Input() {} //We do not want to be able to create an instance of this class

	//region key maps (pressed, typed, released)
	/** A {@code HashMap} to keep track of the keys pressed in the game loop. Mutually exclusive to {@code keysReleased} */
	private HashMap<KeyCode, Boolean> keysPressed  = new HashMap<>();
	/** A {@code HashMap} to keep track of the keys typed in the game loop. Should be reset every loop */
	private HashMap<KeyCode, Boolean> keysTyped    = new HashMap<>();
	/** A {@code HashMap} to keep track of the keys released in the game loop. Mutually exclusive to {@code keysPressed} */
	private HashMap<KeyCode, Boolean> keysReleased = new HashMap<>();
	//endregion

	//region mouse maps (pressed, clicked, released)
	/** A {@code HashMap} to keep track of the mouse buttons pressed in the game loop. Mutually exclusive to {@code mouseButtonReleased} */
	private HashMap<MouseButton, Boolean> mouseButtonPressed  = new HashMap<>();
	/** A {@code HashMap} to keep track of the mouse buttons clicked in the game loop. Should be reset every loop */
	private HashMap<MouseButton, Boolean> mouseButtonClicked  = new HashMap<>();
	/** A {@code HashMap} to keep track of the mouse buttons released in the game loop. Mutually exclusive to {@code mouseButtonPressed} */
	private HashMap<MouseButton, Boolean> mouseButtonReleased = new HashMap<>();
	//endregion

	/** A {@code Vector2} to store the position of the mouse on the screen */
	private Vector2 mousePosition = new Vector2();

	//region initializer
	{
		for (KeyCode kc : KeyCode.values()) {
			keysPressed.put(kc, false);
			keysTyped.put(kc, false);
			keysReleased.put(kc, true);
		}

		for (MouseButton mb : MouseButton.values()) {
			mouseButtonPressed.put(mb, false);
			mouseButtonClicked.put(mb, false);
			mouseButtonReleased.put(mb, true);
		}
	}
	//endregion

	//region isKey methods (Pressed, Typed, up)
	/**
	 * A method to return whether a given key is in the pressed state. Should be {@code false} by default
	 *
	 * @param kc The {@code KeyCode} to check
	 *
	 * @return {@code true} if the key is in the pressed state, and {@code false} otherwise
	 *
	 * @see KeyCode
	 */
	public boolean isKeyPressed(KeyCode kc) {
		return keysPressed.get(kc);
	}
	/**
	 * A method to return whether a given key is in the typed state
	 *
	 * @param kc The {@code KeyCode} to check
	 *
	 * @return {@code true} if the key is in the typed state, and {@code false} otherwise
	 *
	 * @see KeyCode
	 */
	public boolean isKeyTyped(KeyCode kc) {
		return keysTyped.get(kc);
	}
	/**
	 * A method to return whether a given key is in the released state. Should be {@code true} by default
	 *
	 * @param kc The {@code KeyCode} to check
	 *
	 * @return {@code true} if the key is in the released state, and {@code false} otherwise
	 *
	 * @see KeyCode
	 */
	public boolean isKeyReleased(KeyCode kc) {
		return keysReleased.get(kc);
	}
	//endregion

	//region Set Key values
	/**
	 * A method to set the state of the given key as pressed
	 *
	 * @param kc The {@code KeyCode} to set to true. Whichever code is set, the same value is set to {@code false} in the {@code
	 *           keysReleased} map to preserve the mutually exclusive nature of these events
	 *
	 * @see KeyCode
	 */
	public void setKeyPressed(KeyCode kc) {
		keysPressed.put(kc, true);
		keysReleased.put(kc, false);
	}
	/**
	 * A method to set the state of the given key as typed
	 *
	 * @param kc The {@code KeyCode} to set to true. This should be reset to false at the end of every game loop as there is no other
	 *           mechanism to set it back to false unlike the {@code setKeyPressed} and {@code setKeyReleased} methods
	 *
	 * @see KeyCode
	 */
	public void setKeyTyped(KeyCode kc) { keysTyped.put(kc, true); }
	/**
	 * A method to set the state of a given key as released
	 *
	 * @param kc The {@code KeyCode} to set to true. Whichever code is set, the same value is set to {@code false} in the {@code
	 *           keysPressed} map to preserve the mutually exclusive nature of these events
	 *
	 * @see KeyCode
	 */
	public void setKeyReleased(KeyCode kc) {
		keysReleased.put(kc, true);
		keysPressed.put(kc, false);
	}
	//endregion

	//region Set Button Values
	/**
	 * A method to set the state of the given button as pressed
	 *
	 * @param mb The {@code MouseButton} to set to true. Whichever button is set, the same value is set to {@code false} in the {@code
	 *           mouseButtonReleased} map to preserve the mutually exclusive nature of these events
	 *
	 * @see MouseButton
	 */
	public void setButtonPressed(MouseButton mb) {
		mouseButtonPressed.put(mb, true);
		mouseButtonReleased.put(mb, false);
	}
	/**
	 * A method to set the state of the given button as clicked
	 *
	 * @param mb The {@code MouseButton} to set to true. This should be reset to false at the end of every game loop as there is no other
	 *           mechanism to set it back to false unlike the {@code setButtonPressed} and {@code setButtonReleased} methods
	 *
	 * @see MouseButton
	 */
	public void setButtonClicked(MouseButton mb) { mouseButtonClicked.put(mb, true); }
	/**
	 * A method to set the state of the given button as released
	 *
	 * @param mb The {@code MouseButton} to set to true. Whichever button is set, the same value is set to {@code false} in the {@code
	 *           mouseButtonPressed} map to preserve the mutually exclusive nature of these events
	 *
	 * @see MouseButton
	 */
	public void setButtonReleased(MouseButton mb) {
		mouseButtonReleased.put(mb, true);
		mouseButtonPressed.put(mb, false);
	}
	//endregion

	//region isButton methods (Pressed, Clicked, Released)
	/**
	 * A method to return whether a given mouse button is in the pressed state. Should be {@code false} by default
	 *
	 * @param mb The {@code MouseButton} to check
	 *
	 * @return {@code true} if the button is in the pressed state, and {@code false} otherwise
	 *
	 * @see MouseButton
	 */
	public boolean isButtonPressed(MouseButton mb) { return mouseButtonPressed.get(mb); }
	/**
	 * A method to return whether a given mouse button is in the clicked state
	 *
	 * @param mb The {@code MouseButton} to check
	 *
	 * @return {@code true} if the button is in the clicked state, and {@code false} otherwise
	 *
	 * @see MouseButton
	 */
	public boolean isButtonClicked(MouseButton mb) { return mouseButtonClicked.get(mb); }
	/**
	 * A method to return whether a given mouse button is in the released state. Should be {@code true} by default
	 *
	 * @param mb The {@code MouseButton} to check
	 *
	 * @return {@code true} if the button is in the released state, and {@code false} otherwise
	 *
	 * @see MouseButton
	 */
	public boolean isButtonReleased(MouseButton mb) { return mouseButtonReleased.get(mb); }
	//endregion

	//region Get/Set mousePosition
	/**
	 * A method to return the {@code Vector2} object representing the mouse position in the game
	 *
	 * @return The position of the mouse in a {@code Vector2}
	 *
	 * @see Vector2
	 */
	public Vector2 getMousePosition() { return mousePosition; }
	/**
	 * A method to set the position of the mouse using a {@code Vector2}
	 *
	 * @param mousePosition The position of the mouse encoded in a {@code Vector2}
	 *
	 * @see Vector2
	 */
	public void setMousePosition(Vector2 mousePosition) { this.mousePosition = mousePosition; }
	/**
	 * A method to set the position of the mouse using two {@code double}s, given with an {@code x} and a {@code y}
	 *
	 * @param x The x position of the mouse
	 * @param y The y position of the mouse
	 */
	public void setMousePosition(double x, double y) { mousePosition.setXY(x, y); }
	//endregion

	//region resetValues (keyboard and mouse maps)
	/**
	 * A method to reset the {@code keysTyped} and {@code mouseButtonClicked} maps as they don't have a mechanism to reset themselves
	 * automatically
	 */
	public void resetValues() {
		for (KeyCode kc : KeyCode.values())
			keysTyped.replace(kc, false);
		for (MouseButton mb : MouseButton.values())
			mouseButtonClicked.replace(mb, false);
	}
	//endregion

	/** The method used to return the static instance this class holds of itself. Part of the singleton pattern */
	public synchronized static Input getInstance() {
		if (instance == null)
			instance = new Input();
		return instance;
	}
}