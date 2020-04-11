package com.halflife3.Controller.Input;

import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;

/**
 * A class (like {@code MouseInput}) used to handle key input events for the scene. This just tidies up having to declare the handle in the
 * game modes, and makes use of the {@code Input} class.
 * <p>
 * It works by being passed as an event listener to the scene, and taking all the key events (via the {@code KeyEvent.ANY} constant) when
 * creating the event handler. The event type is then determined via the passed event, and the necessary method(s) are invoked in {@code
 * Input} and passed the {@code KeyCode} of the event.
 * <p>
 * This design keeps the code neatly integrated, and means that changing the functionality of the program can be done very easily as it is
 * nicely separated and decoupled. In the event more functionality is required in the future, the required methods and data structures can
 * be added to Input, and then called in here to link them up.
 *
 * @see KeyEvent
 * @see javafx.scene.input.KeyCode
 * @see MouseInput
 * @see Input
 */
public class KeyboardInput implements EventHandler<KeyEvent> {
	@Override
	public void handle(KeyEvent keyEvent) {
		if (keyEvent.getEventType().equals(KeyEvent.KEY_PRESSED)) {
			Input.getInstance().setKeyPressed(keyEvent.getCode());
		} else if (keyEvent.getEventType().equals(KeyEvent.KEY_RELEASED)) {
			Input.getInstance().setKeyReleased(keyEvent.getCode());
		} else if (keyEvent.getEventType().equals(KeyEvent.KEY_TYPED)) {
			Input.getInstance().setKeyTyped(keyEvent.getCode());
		}
	}
}