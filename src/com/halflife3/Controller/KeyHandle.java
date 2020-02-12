package com.halflife3.Controller;

import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;

public class KeyHandle implements EventHandler<KeyEvent> {
    public Input input = new Input();
    /*
    * Add some change: if one key was released, just that key to false
    * in the keyspressed list.
    * */
    @Override

    public void handle(KeyEvent keyEvent) {
        if(keyEvent.getEventType() == KeyEvent.KEY_PRESSED){
            input.keysPressed.replace(keyEvent.getCode(), true);
        } else if(keyEvent.getEventType() == KeyEvent.KEY_RELEASED) {
            input.keysPressed.replace(keyEvent.getCode(), false);
        } else if(keyEvent.getEventType() == KeyEvent.KEY_TYPED) {
            input.keysTyped.replace(keyEvent.getCode(), true);
        }
    }

    {
        //TODO: Initialise input value with a global shared Input reference in the main class

    }
}