package com.halflife3.Controller.Input;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

/**
 * A class (like {@code KeyboardInput}) used to handle mouse input events for the scene. This just tidies up having to declare the handle in
 * the game modes, and makes use of the {@code Input} class.
 * <p>
 * It works by being passed as an event listener to the scene, and taking all the mouse (via the {@code MouseEvent.ANY} constant) events
 * when creating the event handler. The event type is then determined via the passed event, and the necessary method(s) are invoked in
 * {@code Input} and passed the {@code MouseButton} of the event.
 * <p>
 * This design keeps the code neatly integrated, and means that changing the functionality of the program can be done very easily as it is
 * nicely separated and decoupled. In the event more functionality is required in the future, the required methods and data structures can
 * be added to Input, and then called in here to link them up.
 *
 * @see MouseEvent
 * @see javafx.scene.input.MouseButton
 * @see KeyboardInput
 * @see Input
 */
public class MouseInput implements EventHandler<MouseEvent> {
	@Override
	public void handle(MouseEvent mouseEvent) {
		if (mouseEvent.getEventType() == MouseEvent.MOUSE_PRESSED) {
			Input.getInstance().setButtonPressed(mouseEvent.getButton());
		} else if (mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED) {
			Input.getInstance().setButtonClicked(mouseEvent.getButton());
		} else if (mouseEvent.getEventType() == MouseEvent.MOUSE_RELEASED) {
			Input.getInstance().setButtonReleased(mouseEvent.getButton());
		} else if (mouseEvent.getEventType() == MouseEvent.MOUSE_MOVED || mouseEvent.getEventType() == MouseEvent.MOUSE_DRAGGED) {
			Input.getInstance().setMousePosition(mouseEvent.getSceneX(), mouseEvent.getSceneY());
		} //Add any more mouse events in here that we need
	}
}