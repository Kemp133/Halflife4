package com.halflife3.Controller;

import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;

public class KeyboardInput implements EventHandler<KeyEvent> {
    public KeyboardInput() {}

    @Override
    public void handle(KeyEvent keyEvent) {
        if (keyEvent.getEventType().equals(KeyEvent.KEY_PRESSED)) {
            Input.getInstance().setKeyPressed(keyEvent.getCode()); //Mutual exclusion now dealt with in Input
        } else if (keyEvent.getEventType().equals(KeyEvent.KEY_RELEASED)) {
            Input.getInstance().setKeyReleased(keyEvent.getCode());
        } else if (keyEvent.getEventType().equals(KeyEvent.KEY_TYPED)) {
            Input.getInstance().setKeyTyped(keyEvent.getCode());
        } //Add more here in the event that's needed
    }
}