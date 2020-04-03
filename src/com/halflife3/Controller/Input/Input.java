package com.halflife3.Controller.Input;

import com.halflife3.Mechanics.Vector2;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

import java.util.HashMap;

/**
 * @author Team Halflife
 *
 * This class deals with the input for the program. It is a singleton pattern, meaning that it is accessible from
 * anywhere, but all the contents do not need to be static themselves. This class contains hash maps to act as a mapping
 * between input keycodes, and what state they're in. There are hash maps for three types of input, and this exists for
 * both keyboard and mouse inputs. This class also stores the mouse position in a {@code Vector2} object, keeping all
 * input based logic neatly encapsulated in one class.
 *
 * These consist of pressed, momentary (typed and clicked respectively), and released. Due to the pressed and released
 * values being mutually exclusive of each other, when a key is pressed (and hence the keyvalue in the pressed hash map
 * is set to true), the corresponding entry in the released hash map is also then set to false. This means that only the
 * typed values need to be reset each loop, which reduces the amount of time spent in the input class.
 *
 * On creation (i.e the first time the getInstance method is called), the values in the hash map are initialised once
 * and then are ready to use. The getInstance method is also classed as enthusiastic initilisation as if the instance
 * doesn't exist, it's cretated inside of this method
 */
public class Input {
	public static Input instance; //Used as part of singleton design of this class
	private Input () {} //We do not want to be able to create an instance of this class

	//region key maps (pressed, typed, released)
	private HashMap<KeyCode, Boolean> keysPressed  = new HashMap<>();
	private HashMap<KeyCode, Boolean> keysTyped    = new HashMap<>();
	private HashMap<KeyCode, Boolean> keysReleased = new HashMap<>();
	//endregion

	//region mouse maps (pressed, clicked, released)
	private HashMap<MouseButton, Boolean> mouseButtonPressed  = new HashMap<>();
	private HashMap<MouseButton, Boolean> mouseButtonClicked  = new HashMap<>();
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
	public boolean isKeyPressed (KeyCode kc) {
		return keysPressed.get(kc);
	}
	public boolean isKeyTyped (KeyCode kc) {
		return keysTyped.get(kc);
	}
	public boolean isKeyReleased (KeyCode kc) {
		return keysReleased.get(kc);
	}
	//endregion

	//region Set Key values
	public void setKeyPressed (KeyCode kc) {
		keysPressed.put(kc, true);
		keysReleased.put(kc, false);
	}
	public void setKeyTyped (KeyCode kc) { keysTyped.put(kc, true); }
	public void setKeyReleased (KeyCode kc) {
		keysReleased.put(kc, true);
		keysPressed.put(kc, false);
	}
	//endregion

    //region Set Button Values
    public void setButtonPressed(MouseButton mb) {
	    mouseButtonPressed.put(mb, true);
	    mouseButtonReleased.put(mb, false);
    }
    public void setButtonClicked(MouseButton mb) { mouseButtonClicked.put(mb, true); }
    public void setButtonReleased(MouseButton mb) {
	    mouseButtonReleased.put(mb, true);
	    mouseButtonPressed.put(mb, false);
    }
    //endregion

	//region isButton methods (Pressed, Clicked, Released)
	public boolean isButtonPressed (MouseButton mb) { return mouseButtonPressed.get(mb); }
	public boolean isButtonClicked (MouseButton mb) { return mouseButtonClicked.get(mb); }
	public boolean isButtonReleased (MouseButton mb) { return mouseButtonReleased.get(mb); }
	//endregion

	//region Get/Set mousePosition
	public Vector2 getMousePosition () { return mousePosition; }
	public void setMousePosition (Vector2 mousePosition) { this.mousePosition = mousePosition; }
	public void setMousePosition(double x, double y) { mousePosition.setXY(x, y); }
	//endregion

	//region resetValues (keyboard and mouse maps)
	public void resetValues () {
		for (KeyCode kc : KeyCode.values())
			keysTyped.replace(kc, false);   //Don't need to reset key pressed/released as these are mutually exclusive events which are dealt with in the KeyboardInput handler
		for (MouseButton mb : MouseButton.values())
			mouseButtonClicked.replace(mb, false); //Don't need to reset mouse pressed/release as these are mutually exclusive events which are dealt with in the MouseInput handler
	}
	//endregion

	public synchronized static Input getInstance () {
		if (instance == null)
			instance = new Input();

		return instance;
	}
}