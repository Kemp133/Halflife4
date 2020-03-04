package com.halflife3.Controller;

import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;

public class KeyboardInput implements EventHandler<KeyEvent> {

    public KeyboardInput() {}

    @Override
    public void handle(KeyEvent keyEvent) {
//        System.out.println(String.format("Key %s was pressed!", keyEvent.getCode().toString()));
        if (keyEvent.getEventType().equals(KeyEvent.KEY_PRESSED)) {
            Input.keysPressed.replace(keyEvent.getCode(), true);
            Input.keysReleased.replace(keyEvent.getCode(), false);
        } else if (keyEvent.getEventType().equals(KeyEvent.KEY_RELEASED)) {
            Input.keysPressed.replace(keyEvent.getCode(), false);
            Input.keysReleased.replace(keyEvent.getCode(), true);
        } else if (keyEvent.getEventType().equals(KeyEvent.KEY_TYPED)) {
            Input.keysTyped.replace(keyEvent.getCode(), true);
        }
    }
}
