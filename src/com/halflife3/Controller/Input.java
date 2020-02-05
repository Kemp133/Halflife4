package com.halflife3.Controller;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.util.HashMap;

public class Input {
    //region key maps (pressed, typed, released)
    public HashMap<KeyCode, Boolean> keysPressed = new HashMap<>();
    public HashMap<KeyCode, Boolean> keysTyped = new HashMap<>();
    public HashMap<KeyCode, Boolean> keysReleased = new HashMap<>();
    //endregion

    //region mouse maps (pressed, clicked, released)
    public HashMap<MouseButton, Boolean> mouseButtonPressed = new HashMap<>();
    public HashMap<MouseButton, Boolean> mouseButtonClicked = new HashMap<>();
    public HashMap<MouseButton, Boolean> mouseButtonReleased = new HashMap<>();
    //endregion

    //region static initializer
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
    //endregion

    //region isKey methods (Pressed, Typed, up)
    public boolean isKeyPressed(KeyCode kc) {
        return keysPressed.get(kc);
    }
    public boolean isKeyTyped(KeyCode kc) {
        return keysTyped.get(kc);
    }
    public boolean isKeyUp(KeyCode kc) {
        return keysReleased.get(kc);
    }
    //endregion

    //region resetValues (keyboard and mouse maps)
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
    //endregion
}


class KeyboardInput implements EventHandler<KeyEvent> {
    Input input;

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


class MouseInput implements EventHandler<MouseEvent> {
    Input input;

    @Override
    public void handle(MouseEvent mouseEvent) {
        if(mouseEvent.getEventType() == MouseEvent.MOUSE_PRESSED) {
            input.mouseButtonPressed.replace(mouseEvent.getButton(), true);
        } else if (mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED) {
            input.mouseButtonClicked.replace(mouseEvent.getButton(), true);
        } else if (mouseEvent.getEventType() == MouseEvent.MOUSE_RELEASED) {
            input.mouseButtonReleased.replace(mouseEvent.getButton(), true);
        }
        //TODO: There are so many different kinds of mouse event that happen, didn't have time to figure the rest out
    }

    {
        //TODO: Initialise input value with a global shared Input reference in the main class
    }
}
