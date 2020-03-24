package com.halflife3.Controller;

import com.halflife3.Model.Vector2;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

import java.util.HashMap;

public class Input {
    //region key maps (pressed, typed, released)
    public static HashMap<KeyCode, Boolean> keysPressed = new HashMap<>();
    public static HashMap<KeyCode, Boolean> keysTyped = new HashMap<>();
    public static HashMap<KeyCode, Boolean> keysReleased = new HashMap<>();
    //endregion

    //region mouse maps (pressed, clicked, released)
    public static HashMap<MouseButton, Boolean> mouseButtonPressed = new HashMap<>();
    public static HashMap<MouseButton, Boolean> mouseButtonClicked = new HashMap<>();
    public static HashMap<MouseButton, Boolean> mouseButtonReleased = new HashMap<>();
    //endregion

    public static Vector2 mousePosition = new Vector2(0,0);

    //region static initializer
    static {
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
    public static boolean isKeyPressed(KeyCode kc) {
        return keysPressed.get(kc);
    }
    public static boolean isKeyTyped(KeyCode kc) {
        return keysTyped.get(kc);
    }
    public static boolean isKeyReleased(KeyCode kc) {
        return keysReleased.get(kc);
    }
    //endregion

    //region resetValues (keyboard and mouse maps)
    public static void resetValues() {
        for (KeyCode kc : KeyCode.values())
            keysTyped.replace(kc, false);   //Don't need to reset key pressed/released as these are mutually exclusive events which are dealt with in the KeyboardInput handler

        for(MouseButton mb : MouseButton.values())
            mouseButtonClicked.replace(mb, false); //Don't need to reset mouse pressed/release as these are mutually exclusive events which are dealt with in the MouseInput handler
    }
    //endregion
}