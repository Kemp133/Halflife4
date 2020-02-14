package com.halflife3.Controller;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

public class MouseInput implements EventHandler<MouseEvent> {
    Input input;

    public MouseInput(Input input) {
        this.input = input;
    }

    @Override
    public void handle(MouseEvent mouseEvent) {
        if(mouseEvent.getEventType() == MouseEvent.MOUSE_PRESSED) {
            input.mouseButtonPressed.replace(mouseEvent.getButton(), true);
            input.mouseButtonReleased.replace(mouseEvent.getButton(), false);
        } else if (mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED) {
            input.mouseButtonClicked.replace(mouseEvent.getButton(), true);
        } else if (mouseEvent.getEventType() == MouseEvent.MOUSE_RELEASED) {
            input.mouseButtonReleased.replace(mouseEvent.getButton(), true);
            input.mouseButtonPressed.replace(mouseEvent.getButton(), false);
        } else if (mouseEvent.getEventType() == MouseEvent.MOUSE_MOVED) {
            input.mousePosition.setX(mouseEvent.getSceneX());
            input.mousePosition.setY(mouseEvent.getSceneY());
        }
        //TODO: There are so many different kinds of mouse event that happen, didn't have time to figure the rest out
    }

    {
        //TODO: Initialise input value with a global shared Input reference in the main class
    }
}
