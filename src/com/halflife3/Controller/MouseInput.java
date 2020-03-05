package com.halflife3.Controller;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

public class MouseInput implements EventHandler<MouseEvent> {

    public MouseInput() {}

    @Override
    public void handle(MouseEvent mouseEvent) {
        if(mouseEvent.getEventType() == MouseEvent.MOUSE_PRESSED) {
            Input.mouseButtonPressed.replace(mouseEvent.getButton(), true);
            Input.mouseButtonReleased.replace(mouseEvent.getButton(), false);
        } else if (mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED) {
            Input.mouseButtonClicked.replace(mouseEvent.getButton(), true);
        } else if (mouseEvent.getEventType() == MouseEvent.MOUSE_RELEASED) {
            Input.mouseButtonReleased.replace(mouseEvent.getButton(), true);
            Input.mouseButtonPressed.replace(mouseEvent.getButton(), false);
        } else if (mouseEvent.getEventType() == MouseEvent.MOUSE_MOVED || mouseEvent.getEventType() == MouseEvent.MOUSE_DRAGGED) {
            Input.mousePosition.setX(mouseEvent.getSceneX());
            Input.mousePosition.setY(mouseEvent.getSceneY());
        }
        //TODO: There are so many different kinds of mouse event that happen, didn't have time to figure the rest out
    }
}
