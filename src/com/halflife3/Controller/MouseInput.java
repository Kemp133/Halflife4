package com.halflife3.Controller;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

public class MouseInput implements EventHandler<MouseEvent> {
    public MouseInput() {}

    @Override
    public void handle(MouseEvent mouseEvent) {
        if(mouseEvent.getEventType() == MouseEvent.MOUSE_PRESSED) {
            Input.getInstance().setButtonPressed(mouseEvent.getButton());
        } else if (mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED) {
            Input.getInstance().setButtonClicked(mouseEvent.getButton());
        } else if (mouseEvent.getEventType() == MouseEvent.MOUSE_RELEASED) {
            Input.getInstance().setButtonReleased(mouseEvent.getButton());
        } else if (mouseEvent.getEventType() == MouseEvent.MOUSE_MOVED || mouseEvent.getEventType() == MouseEvent.MOUSE_DRAGGED) {
            Input.getInstance().setMousePosition(mouseEvent.getSceneX(), mouseEvent.getSceneY());
        } //Add any more mouse events in here that we need
    }
}