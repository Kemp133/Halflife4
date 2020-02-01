package com.halflife3.Controller;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.util.HashMap;

public class Input {
    public HashMap<KeyCode, Boolean> keysPressed = new HashMap<>();
    public HashMap<KeyCode, Boolean> keysTyped = new HashMap<>();
    public HashMap<KeyCode, Boolean> keysReleased = new HashMap<>();

    public HashMap<MouseButton, Boolean> mouseButtonPressed = new HashMap<>();
    public HashMap<MouseButton, Boolean> mouseButtonClicked = new HashMap<>();
    public HashMap<MouseButton, Boolean> mouseButtonReleased = new HashMap<>();

    //TODO: New map for the mouse events which stored the position values

    {
        for (KeyCode kc : KeyCode.values()) {
            keysPressed.put(kc, false);
            keysTyped.put(kc, false);
            keysReleased.put(kc, false);
        }

        for(MouseButton mb: MouseButton.values()) {
            mouseButtonPressed.put(mb, false);
            mouseButtonClicked.put(mb, false);
            mouseButtonReleased.put(mb, false);
        }
    }

    public boolean isKeyPressed(KeyCode kc) {
        return keysPressed.get(kc);
    }

    public boolean isKeyTyped(KeyCode kc) {
        return keysTyped.get(kc);
    }

    public boolean isKeyUp(KeyCode kc) {
        return keysReleased.get(kc);
    }

    public void resetValues() {
        for (KeyCode kc : KeyCode.values()) {
            keysPressed.replace(kc, false);
            keysTyped.replace(kc, false);
            keysReleased.replace(kc, false);
        }

        for(MouseButton mb : MouseButton.values()) {
            mouseButtonPressed.replace(mb, false);
            mouseButtonClicked.replace(mb, false);
            mouseButtonReleased.replace(mb, false);
        }
    }
}




