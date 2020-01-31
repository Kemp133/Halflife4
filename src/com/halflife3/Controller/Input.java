package com.halflife3.Controller;

import javafx.scene.input.KeyCode;

import java.util.HashMap;

public class Input {
    public HashMap<KeyCode, Boolean> keysPressed = new HashMap<>();
    public HashMap<KeyCode, Boolean> keysTyped = new HashMap<>();
    public HashMap<KeyCode, Boolean> keysReleased = new HashMap<>();

    {
        for(KeyCode kc: KeyCode.values()) {
            keysPressed.put(kc, false);
            keysTyped.put(kc, false);
            keysReleased.put(kc, false);
        }
    }

    public boolean isKeyPressed(KeyCode kc) { return keysPressed.get(kc); }
    public boolean isKeyTyped(KeyCode kc) { return keysTyped.get(kc); }
    public boolean isKeyUp(KeyCode kc) { return keysReleased.get(kc); }
}
