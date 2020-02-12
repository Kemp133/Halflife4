package com.halflife3.Controller;

import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;

public class KeyboardInput implements EventHandler<KeyEvent> {
    Input input;

    public KeyboardInput(Input input) {
        this.input = input;
    }

    @Override
    public void handle(KeyEvent keyEvent) {
        System.out.println(String.format("Key %s was pressed!", keyEvent.getCode().toString()));

        if (keyEvent.getEventType().equals(KeyEvent.KEY_PRESSED)) {
            input.keysPressed.replace(keyEvent.getCode(), true);
            input.keysReleased.replace(keyEvent.getCode(), false);
        } else if (keyEvent.getEventType().equals(KeyEvent.KEY_RELEASED)) {
            input.keysPressed.replace(keyEvent.getCode(), false);
            input.keysReleased.replace(keyEvent.getCode(), true);
        } else if (keyEvent.getEventType().equals(KeyEvent.KEY_TYPED)) {
            input.keysTyped.replace(keyEvent.getCode(), true);
        }
    }

    {
        //TODO: Initialise input value with a global shared Input reference in the main class
    }
}
