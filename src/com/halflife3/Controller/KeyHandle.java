package com.halflife3.Controller;

import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;

public class KeyHandle implements EventHandler<KeyEvent> {
    public Input input;

    @Override
    public void handle(KeyEvent keyEvent) {
        if(keyEvent.getEventType() == KeyEvent.KEY_PRESSED){
            input.keysPressed.replace(keyEvent.getCode(), true);
        } else if(keyEvent.getEventType() == KeyEvent.KEY_RELEASED) {
            input.keysReleased.replace(keyEvent.getCode(), true);
        } else if(keyEvent.getEventType() == KeyEvent.KEY_TYPED) {
            input.keysTyped.replace(keyEvent.getCode(), true);
        }
    }

    {
        //TODO: Initialise input value with a global shared Input reference in the main class

    }
}